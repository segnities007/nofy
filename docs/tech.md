# What is this

- これはこのプロダクトの技術選定についてまとめたドキュメントです。

## 採用技術

- 言語
  - Kotlin
- UI
  - Jetpack Compose
  - `platform:designsystem` 経由の独自 UI コンポーネント
- DB
  - Room
- DB暗号化
  - SQLCipher for Android (`net.zetetic:sqlcipher-android`)
- ノート内容暗号化
  - AES-GCM（Android Keystore 管理鍵）
- 鍵保管
  - Android Keystore（プラットフォームAPI）
- 生体認証
  - AndroidX Biometric
- パスフレーズ由来鍵
  - Argon2Kt
- 認証メタデータ保存
  - Android Keystore 保護の custom secure preferences store
- UI設定保存
  - Android Keystore 保護の custom secure preferences store

  
## アーキテクチャ

以下の３つを組み合わせる

1. Clean Architecture
2. Layered Architecture
3. Modular Architecture

それは

1. ビジネスロジックが他に依存しない(ビジネスロジックに依存を向ける)ために
2. ビジネスロジックをdomain、他の部分をpresentationやdataといった責務で分けて
3. それらをfeature単位のモジュールとして分ける

結果、保守性を高く維持しつつ、拡張性を高めた物にしています。

認証まわりは `shared:auth` を単一の契約/実装モジュールとして扱い、`feature:login` や `feature:setting` はその API のみを利用します。これにより feature 側で認証ロジックを重複させず、依存方向を shared domain に統一します。

app module では依存生成を `NofyApplication` の `startKoin { modules(...) }` に集約します。依存定義は module ごとの Koin module として宣言し、`MainActivity` は theme state の購読、`NofyNavHost` は navigation 配線に責務を限定します。repository / cipher / storage / `ViewModel` を composable や Activity で `new` しません。

秘密データを扱う都合上、app root では `FLAG_SECURE` と process lifecycle による auto-lock を必須とします。window の secure 化は `MainActivity` で 1 回だけ行い、app が background に入ったら `shared:auth` を通して DB を lock し、navigation も login へ戻します。Android 12+ では `HIDE_OVERLAY_WINDOWS` と `setHideOverlayWindows(true)`、Android 13+ では `setRecentsScreenshotEnabled(false)` も併用し、overlay 攻撃や recents screenshot 露出の成立条件を下げます。foreground に留まる場合でも機密画面は一定時間の無操作で auto-lock します。

パスワード認証には local brute-force 対策を入れます。失敗回数と lockout deadline は `platform:storage` に保持し、`shared:auth` が owner として段階的な lockout を適用します。biometric unlock 成功時と password unlock 成功時は失敗回数を必ず clear します。

生体認証鍵は `setInvalidatedByBiometricEnrollment(true)` を前提に扱います。つまり enrollment 変更や secure lock 設定変更で鍵が使えなくなるのは正常系です。`feature:login` はこの状態を generic failure にせず、「保存済み biometric secret を破棄して再登録を要求する」typed flow として扱います。`feature:setting` で biometric login を無効化した場合も secret/IV は即時削除し、再有効化では current password の再確認と biometric prompt による再登録を必須にします。`CryptoObject` を使う biometric unlock / enrollment は `BIOMETRIC_STRONG` が使える端末だけを対象にし、device credential のみを strong biometric の代替として扱いません。

アプリ用パスワードは「ただ空でなければよい」とは扱いません。`shared:auth` の password policy を通して最小長と blocklist を強制し、`platform:crypto` の `PasswordHasher` は NFC 正規化した入力を Argon2id で計算したあと、Android Keystore 保護の HMAC key で pepper 化して保存します。これは NIST が推奨する「salt + iterated hash + optional keyed hash」を、ローカル vault の threat model に合わせて強めたものです。

オフラインアプリのため、server-side verify を前提にした `Play Integrity` は採用しません。その代わり release build では app root と機微操作の直前の両方で risky environment detection を行います。`app` は foreground 中も短い間隔で監視し、debugger 接続、`TracerPid`、Frida 既知ポート、`/proc/self/maps` 上の hook library、Xposed/LSPosed/Substrate 系 package、`LD_PRELOAD`、unexpected debuggable app、test-keys firmware、writable system partition、SELinux permissive、既知の root artifact / root manager を検出した場合は即 lock と block screen へ遷移します。`shared:auth` と `feature:note` は `SensitiveOperationGuard` を通して unlock、password change、reset、biometric secret の読み書き、note の read/write/delete を fail-closed に止めます。これは完全な耐タンパーではなく heuristic ですが、オフライン前提で取り得る現実的な hardening として扱います。

アプリ全体の `NofyTheme` 適用は `MainActivity` の root で 1 回だけ行います。feature の screen は runtime で theme を巻き直さず、`NofySurface` と screen content の描画、effect 収集、navigation 配線に責務を限定します。Preview のみ screen 側で `NofyTheme` を利用します。

各 feature の screen は `koinViewModel()` を使って `ViewModel` を取得します。`ViewModelProvider.Factory` や `viewModelFactory` helper は使わず、依存の定義は Koin module 側へ寄せます。`Activity` や prompt 文言のような runtime 値が必要な場合だけ `parametersOf(...)` を使い、screen は配線に責務を限定します。

`ViewModel` や state holder の副作用コードは、宣言的に近い orchestration を基本にします。上位関数では `load / validate / persist / reduce / handleFailure` のように「何をするか」だけを書き、repository 呼び出し、state 更新、effect 発火、domain→UI 変換は named helper へ分離します。`onSuccess` / `onFailure` / `launch` の中へ長い処理を埋め込まず、guard clause で正常フローを左に寄せます。

画面内の widget state も composable へ散在させず、責務が複数ある場合は plain state holder へ寄せます。たとえば `feature:note` では pager chrome の表示制御と削除確認 dialog の対象管理を screen state holder に集約し、screen composable 自体は state 購読、effect 収集、navigation 配線、section composable の組み立てに限定します。

宣言的オーケストレーションを維持するために、ロジックは次の 3 層へ分けます。

1. `ViewModel`
2. `UseCase`
3. `Operation`

`ViewModel` は screen 単位の宣言的オーケストレーションだけを持ちます。責務は `Intent` の受付、guard clause による入力の絞り込み、`viewModelScope.launch` の開始、`start... / execute... / reduce...` の順で上位フローを並べること、`UiState` と `Effect` の公開に限定します。`ViewModel` の上位関数は 3〜6 行程度を目安にし、repository 呼び出し、callback 処理、暗号化、Android API 呼び出しを直接書きません。

`UseCase` はビジネス上意味のある 1 操作を表します。複数 repository の協調、domain validation、永続化、typed result への変換を担当します。`UseCase` は `feature/*/domain/usecase` または `shared/*/domain/usecase` に置き、`Context`、`stringResource`、navigation、Toast、`BiometricPrompt`、`CryptoObject`、Compose state を持ち込みません。戻り値は `Result<Unit>` のような曖昧な形より、`sealed interface` や専用 result model を優先します。

`Operation` は presentation/integration 寄りの命令的処理を隔離するための helper です。Android framework、callback API、biometric prompt、cipher 準備、`Flow.first()` のような「実行手順が目立つ処理」をここへ閉じ込めます。`Operation` は `feature/*/presentation/operation` または近接 file の private top-level helper として置き、`ViewModel` からは `execute...()` として呼びます。これにより、命令的コードは深掘りしない限り見えません。

判断基準は次の通りです。

- domain 用語で名前が付くなら `UseCase` に置く。例: `LoadNotesUseCase`, `ChangePasswordUseCase`, `ResetAppUseCase`
- Android API や callback を触るなら `Operation` に置く。例: `StartBiometricUnlockOperation`, `EncryptPasswordForBiometricOperation`
- 「何をどの順で行うか」を読むための関数なら `ViewModel` に置く

`ViewModel` の推奨形は次の通りです。

```kotlin
private fun loadNotes() {
    viewModelScope.launch {
        startNotesLoad()
        val result = executeLoadNotes()
        reduceNotesLoad(result)
    }
}
```

ここで `start...` は loading などの事前 state 更新、`execute...` は `UseCase` または `Operation` の呼び出し、`reduce...` は成功/失敗を UI state と effect へ落とし込む処理です。1 関数の中でこれら 3 つ以上の抽象度を混在させません。

biometric のように domain と Android 依存が混ざる処理は分割して扱います。たとえば「暗号化済み secret を保存する」は `UseCase`、「`BiometricPrompt` を表示して `CryptoObject` を受け取る」は `Operation`、「登録成功後にどちらを先に呼び、どの state を更新するか」は `ViewModel` が担当します。

機密入力の寿命は最小化します。password のような平文入力は `ViewModel` の長寿命 state に保持せず、screen / section の local UI state に閉じ込め、submit 後はすぐ破棄します。`platform:crypto` の暗号化処理でも UTF-8 byte array は `finally` で都度 zero fill します。解錠済みノートの in-memory state も lock または untrusted environment 検出時には即時 wipe し、back stack 遷移だけに頼って平文 state を残しません。

依存サプライチェーン対策として、Gradle dependency verification を有効化し、`gradle/verification-metadata.xml` で checksum を固定します。新しい dependency を追加・更新する時だけ metadata を更新します。

## Presentation File Layout

presentation 配下では `util` や `mvi` のような曖昧な箱を作らず、役割名そのものを directory 名に使います。公開入口は `screen`、契約は `contract`、状態 helper は `state`、画面 UI は `component/*` に分けます。

- `presentation/contract`: `State` / `Intent` / `Effect` / section enum のような screen contract
- `presentation/viewmodel`: screen 単位の state holder
- `presentation/navigation`: entry installer と navigation 配線
- `presentation/state`: plain state holder、UI state 変換、title extractor、list transition helper
- `presentation/screen`: public screen composable、effect 収集、navigation 配線、preview
- `presentation/component/section`: hero、form、summary、settings section のような意味単位
- `presentation/component/bar`: top bar / bottom bar / pager controls
- `presentation/component/dialog`: dialog / sheet / confirmation UI
- `presentation/component/content`: editor / preview / list body のような本体 UI
- `presentation/component/layout`: screen 直下の scaffold や layout 骨格
- `presentation/biometric`: biometric handler と prompt 構築
- `presentation/operation`: Android callback や prompt など命令的 helper

分割基準は次の通りです。

- UI を描画する file には必ず preview を置く。同 file か近接 file でよいが、preview 無しの UI file を残さない
- preview は `@NofyPreview` と `NofyPreviewSurface` を基本にし、feature ごとの sample state を使って self-contained に書く
- `util` `support` `mvi` `reducer` のような曖昧名は避け、`contract` `state` `bar` `dialog` のような責務名へ置き換える
- `State/Intent/Effect` は root へ平置きせず `presentation/contract` にまとめる
- `ViewModel` と `Navigation` は root へ平置きせず、それぞれ `presentation/viewmodel` と `presentation/navigation` に分ける
- `screen` は公開入口だけにし、画面本体は `component/layout` か `component/content` へ出す
- `LazyColumn` の item 群が 2 セクション以上ある場合は `component/section` へ分ける
- hero、form、dialog、toolbar のように名前で呼べる塊は `component/*` へ独立させる
- package は directory の役割に合わせる。`presentation.component.section` に置いた file を `presentation` root package のまま残さない
- 参照コストを下げるため、screen は `screen.*`、state は `state.*`、contract は `contract.*` を import する構造に統一する

## Target Module Shape

このプロジェクトの最終形は「default は feature に閉じる。共有が実証された責務だけを feature 外へ出す」です。抽象的な `core` を肥大化させず、owner が一意に決まる構造を優先します。

module 配置の原則は次の通りです。

- `app`: Application 起動、Koin 初期化、root navigation の合成だけを持つ
- `feature:*`: その feature だけが持つ UI、domain、data、navigation impl を持つ
- `feature:*:api`: 他 feature から参照される route key や公開 contract を持つ
- `feature:*:impl`: composable、ViewModel、use case、repository impl、entry builder を持つ
- `shared:*`: 2 つ以上の feature が同じ business capability を使う場合のみ置く
- `platform:*`: Android framework や crypto、storage、database bootstrap のような infra を置く

`core` という名前は意味が広すぎるため、新規追加では避けます。既存 module を残す場合でも、責務は `shared` か `platform` のどちらかに寄せます。

Gradle module path は責務を表す `platform:*` / `shared:*` / `feature:*` を使います。物理ディレクトリは段階的に移行してよく、直ちに `core/` 配下の folder 名まで合わせる必要はありません。

### Ownership Rules

- feature 固有の entity / dao / migration / repository impl は feature の data 層に置く
- feature 固有の route と screen は feature が owner になる
- 共有ドメインの契約は `shared:<domain>` が owner になる
- SharedPreferences / DataStore / Room / Keystore / BiometricPrompt のような技術要素は `platform:*` が owner になる
- `ViewModel` は repository か use case までに依存し、dao や storage class へは依存しない

### Recommended Target For This Repo

現行 repo に対する推奨の最終形は次の通りです。

- `app`
- `platform:navigation`
- `platform:designsystem`
- `platform:crypto`
- `platform:storage`
- `platform:database`
- `platform:biometric`
- `shared:auth`
- `shared:settings`
- `feature:login:api`
- `feature:login:impl`
- `feature:note:api`
- `feature:note:impl`
- `feature:setting:api`
- `feature:setting:impl`

### Concrete Migration Direction

#### 1. `feature:note`

`feature:note` は vertical slice に寄せます。`NoteEntity`、`NoteDao`、`NoteDatabase`、notes migration、`NoteRepositoryImpl` は同じ owner である `feature:note` へ集約します。`platform:database` には note schema を置かず、共有側が見る抽象だけを残します。

具体的には次を `feature:note` 側へ移します。

- `NoteEntity`
- `NoteDao`
- `NoteDatabase`
- notes table migration
- `NoteRepositoryImpl`

`platform:database` は次だけを持ちます。

- `SecureDatabaseController` のような shared 抽象
- unlock / lock / delete / passphrase change の lifecycle 契約

#### 2. `shared:auth`

認証は `login` 専用ではなく、`login` と `setting` と app 起動判定で共有されています。そのため owner は feature ではなく shared domain です。旧 `core:auth` に相当する責務は `shared:auth` として扱い、module 名からも owner を明示します。

`shared:auth` は次を持ちます。

- `AuthRepository` 契約
- `AuthRepositoryImpl`
- auth 用 domain error / model
- password hash、biometric secret、database unlock/lock、password change rekey の orchestration
- biometric credential 無効化時の secret 破棄と enabled state の整合維持

ただし Android storage 実装詳細は `platform:storage` に切り出します。

#### 3. `shared:settings`

設定は `setting` screen だけでなく app root theme にも使われます。そのため owner は shared domain です。`UiSettingsRepository` 契約は shared に置き、保存技術は `platform:storage` に置きます。

`shared:settings` は次を持ちます。

- `UiSettings`
- `ThemeMode`
- `UiSettingsRepository`

`platform:storage` は次を持ちます。

- `SettingsLocalDataSource`
- `AuthLocalDataSource`
- `EncryptedPreferences` の実装

#### 4. `platform:storage`

現行 `SecureAuthStorage` は auth と settings を同時に持っており、cohesion が低いです。次の 2 つへ分割します。

- `AuthLocalDataSource`
- `SettingsLocalDataSource`

呼び出し側は implementation detail を知らないようにし、技術名ではなく責務名で依存します。

#### 5. Navigation

Navigation 3 の公式方針に従い、各 feature は `api` と `impl` を分けます。`api` は route key、`impl` は `EntryProviderScope` extension と screen 実装を持ちます。app は feature entry builder を直接 import せず、DI で集めた installer を合成するだけにします。

### Migration Order

移行は次の順で進めます。

1. `platform:storage` を作り、`SecureAuthStorage` を auth/settings に分割する
2. `shared:auth` と `shared:settings` を明示し、旧 `core:auth` / `core:settings` 相当の責務を整理する
3. `feature:note` へ `NoteEntity` / `NoteDao` / migration を戻し、`platform:database` を bootstrap 専用へ縮小する
4. `feature:*` を `api` / `impl` に分割し、navigation entry を impl 側へ閉じる
5. 最後に Gradle module path を `platform:*` / `shared:*` / `feature:*` に統一する

### Decision Heuristic

新しいコードの置き場所に迷った時は次で判断します。

- そのコードを削ると 1 feature しか困らないなら `feature`
- 2 つ以上の feature が同じ business meaning で使うなら `shared`
- Android API や library integration の詳細なら `platform`
- owner が説明できないなら module 分割が間違っている

ノート機能は `feature:note` で `HorizontalPager` を使った単一ユーザー向けの動的ページ UI を提供します。各ページは markdown 編集 / preview を切り替えられ、スクロール時には floating な TopBar / BottomBar を自動で出し分けます。永続化時は Room + SQLCipher で DB 全体を暗号化し、さらにノート本文は Android Keystore 鍵で AES-GCM 暗号化したうえで保存します。field encryption の鍵利用は app の unlock session に従属させ、`shared:auth` の `lock/unlock/reset` と同じタイミングで `DataCipher` も開閉します。これにより、アプリが lock 状態のときは本文の追加復号・再暗号化を拒否します。ページタイトルは本文先頭から導出し、平文カラムとしては保持しません。

feature モジュールでは `androidx.compose.material*` を直接利用せず、`platform:designsystem` が提供する button / text / bar / dialog / markdown などのラッパー経由で UI を構築します。build-logic でも direct import を禁止する検証を入れています。

機密入力や破壊的操作の UI では、design system component に obscured-touch 防御を持たせます。password field、password 更新ボタン、biometric enable toggle、delete / reset confirm のような操作は、`FLAG_WINDOW_IS_OBSCURED` / `FLAG_WINDOW_IS_PARTIALLY_OBSCURED` を検知した touch を受け付けません。password field は `KeyboardType.Password` と `autoCorrectEnabled = false` を標準にし、IME の予測変換や通常テキスト向け補完へ寄らない入力設定を使います。current password のような機密入力は `rememberSaveable` に載せず、composition memory にのみ保持します。

`platform:designsystem` の粒度は `atom` と `molecule` までに限定します。`atom` は単一責務の視覚プリミティブ、`molecule` は atom を組み合わせた小さな操作単位です。たとえば `FloatingBar` は atom、`FloatingTopBar` / `FloatingBottomBar` と `ConfirmationDialog` は molecule に置きます。画面文脈を持つ section や `Scaffold` のような layout shell は feature module 側で実装します。

一時的な toast/snackbar 用メッセージのような feature 専用 UI effect は `platform:designsystem` へ共通 abstraction を作らず、feature 側の effect 契約で `@StringRes` または `String` として扱います。これにより design system は見た目と再利用 UI に責務を限定します。

認証やノート保存の失敗は、data/domain 層で free-form な文字列 `Exception` として表現せず、typed exception で返します。ユーザー向け文言やリトライ表現は presentation 層の `Effect` / `UiState` で解決し、data 層へ表示責務を漏らしません。

markdown preview は `mikepenz/multiplatform-markdown-renderer` を `platform:designsystem` でラップして利用します。feature 側は `NofyMarkdown` を呼ぶだけにして、描画実装詳細を隠蔽します。

アプリ外観設定は `shared:settings` の契約と `platform:storage` の暗号化実装で管理します。テーマは Light / Dark / Green Light / Green Dark の 4 種を持ち、フォント倍率も含めて `Android Keystore` 鍵で AES-GCM 暗号化した `SharedPreferences` に保存され、アプリ全体へ即時反映されます。Keystore の AES 鍵生成は `StrongBox` を優先し、使えない端末では通常の hardware-backed / software-backed keystore へ自動 fallback します。生体認証用の `BiometricCipher` と password pepper 用 HMAC key も同じ思想で StrongBox 優先 + fallback にし、encrypt path では key を self-heal し、decrypt path では「credential unavailable」を明示的に上位へ返します。app reset は destructive phrase だけでなく current password による fresh-auth を要求し、成功時は auth state / encrypted notes / saved UI settings をまとめて初期化します。

ユーザー向け文言は Android の string resources に寄せ、現状は英語と日本語に対応します。端末またはアプリの言語設定に応じて切り替わります。

バックアップは機密性を優先して無効化します。Auto Backup と Android 12+ の data extraction / device transfer には明示的な exclude を設定し、暗号化データや認証メタデータがクラウドバックアップや端末間転送へ乗らないようにします。

release build では R8 による code shrinking / resource shrinking を有効にし、機密ロジックの reverse engineering コストを下げる方向ではなく、上げる方向へ寄せます。debug 用の fallback log は release path へ残しません。DI / framework logger も debug build のみで初期化し、release path では不要な内部ログを出しません。
