# What is this

- これはこのプロダクトの技術選定についてまとめたドキュメントです。

## 採用技術

- 言語
  - Kotlin
- UI
  - Jetpack Compose
  - `core:designsystem` 経由の独自 UI コンポーネント
- DB
  - Room
- DB暗号化
  - SQLCipher
- ノート内容暗号化
  - AES-GCM（Android Keystore 管理鍵）
- 鍵保管
  - Android Keystore（プラットフォームAPI）
- 生体認証
  - AndroidX Biometric
- パスフレーズ由来鍵
  - Argon2Kt
- 認証メタデータ保存
  - EncryptedSharedPreferences
- UI設定保存
  - EncryptedSharedPreferences

  
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

認証まわりは `core:auth` を単一の契約/実装モジュールとして扱い、`feature:login` や `feature:setting` はその API のみを利用します。これにより feature 側で認証ロジックを重複させず、依存方向を core に統一します。

app module では依存生成を `NofyAppContainer` に集約し、`MainActivity` は theme state の購読、`NofyNavHost` は navigation 配線に責務を限定します。manual DI は残しますが、生成責務を composable へ分散させません。

アプリ全体の `NofyTheme` 適用は `MainActivity` の root で 1 回だけ行います。feature の screen は runtime で theme を巻き直さず、`NofySurface` と screen content の描画、effect 収集、navigation 配線に責務を限定します。Preview のみ screen 側で `NofyTheme` を利用します。

各 feature の screen からは匿名 `ViewModelProvider.Factory` を排除し、`viewModelFactory` + `initializer` による named factory helper を使います。これにより screen は dependency 解決の詳細を持たず、Compose tree のネストも抑制します。

`ViewModel` や state holder の副作用コードは、宣言的に近い orchestration を基本にします。上位関数では `load / validate / persist / reduce / handleFailure` のように「何をするか」だけを書き、repository 呼び出し、state 更新、effect 発火、domain→UI 変換は named helper へ分離します。`onSuccess` / `onFailure` / `launch` の中へ長い処理を埋め込まず、guard clause で正常フローを左に寄せます。

画面内の widget state も composable へ散在させず、責務が複数ある場合は plain state holder へ寄せます。たとえば `feature:note` では pager chrome の表示制御と削除確認 dialog の対象管理を screen state holder に集約し、screen composable 自体は state 購読、effect 収集、navigation 配線、section composable の組み立てに限定します。

宣言的オーケストレーションを維持するために、ロジックは次の 3 層へ分けます。

1. `ViewModel`
2. `UseCase`
3. `Operation`

`ViewModel` は screen 単位の宣言的オーケストレーションだけを持ちます。責務は `Intent` の受付、guard clause による入力の絞り込み、`viewModelScope.launch` の開始、`start... / execute... / reduce...` の順で上位フローを並べること、`UiState` と `Effect` の公開に限定します。`ViewModel` の上位関数は 3〜6 行程度を目安にし、repository 呼び出し、callback 処理、暗号化、Android API 呼び出しを直接書きません。

`UseCase` はビジネス上意味のある 1 操作を表します。複数 repository の協調、domain validation、永続化、typed result への変換を担当します。`UseCase` は `feature/*/domain/usecase` または `core/*/domain/usecase` に置き、`Context`、`stringResource`、navigation、Toast、`BiometricPrompt`、`CryptoObject`、Compose state を持ち込みません。戻り値は `Result<Unit>` のような曖昧な形より、`sealed interface` や専用 result model を優先します。

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

ノート機能は `feature:note` で `HorizontalPager` を使った単一ユーザー向けの動的ページ UI を提供します。各ページは markdown 編集 / preview を切り替えられ、スクロール時には floating な TopBar / BottomBar を自動で出し分けます。永続化時は Room + SQLCipher で DB 全体を暗号化し、さらにノート本文は Android Keystore 鍵で AES-GCM 暗号化したうえで保存します。ページタイトルは本文先頭から導出し、平文カラムとしては保持しません。

feature モジュールでは `androidx.compose.material*` を直接利用せず、`core:designsystem` が提供する button / text / bar / dialog / markdown などのラッパー経由で UI を構築します。build-logic でも direct import を禁止する検証を入れています。

`core:designsystem` の粒度は `atom` と `molecule` までに限定します。`atom` は単一責務の視覚プリミティブ、`molecule` は atom を組み合わせた小さな操作単位です。たとえば `FloatingBar` は atom、`FloatingTopBar` / `FloatingBottomBar` と `ConfirmationDialog` は molecule に置きます。画面文脈を持つ section や `Scaffold` のような layout shell は feature module 側で実装します。

一時的な toast/snackbar 用メッセージのような feature 専用 UI effect は `core:designsystem` へ共通 abstraction を作らず、feature 側の effect 契約で `@StringRes` または `String` として扱います。これにより design system は見た目と再利用 UI に責務を限定します。

認証やノート保存の失敗は、data/domain 層で free-form な文字列 `Exception` として表現せず、typed exception で返します。ユーザー向け文言やリトライ表現は presentation 層の `Effect` / `UiState` で解決し、data 層へ表示責務を漏らしません。

markdown preview は `mikepenz/multiplatform-markdown-renderer` を `core:designsystem` でラップして利用します。feature 側は `NofyMarkdown` を呼ぶだけにして、描画実装詳細を隠蔽します。

アプリ外観設定は `core:settings` の契約と `core:datastore` の暗号化実装で管理します。テーマは Light / Dark / Green Light / Green Dark の 4 種を持ち、フォント倍率も含めて `EncryptedSharedPreferences` に保存され、アプリ全体へ即時反映されます。

ユーザー向け文言は Android の string resources に寄せ、現状は英語と日本語に対応します。端末またはアプリの言語設定に応じて切り替わります。

バックアップは機密性を優先して無効化します。Auto Backup と Android 12+ の data extraction / device transfer には明示的な exclude を設定し、暗号化データや認証メタデータがクラウドバックアップや端末間転送へ乗らないようにします。
