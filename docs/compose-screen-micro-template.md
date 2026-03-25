# Compose 画面: ミクロ関心事の抽象と対応テンプレート

`AGENTS.md` §4 と整合する。**行数が理由ではない。** 各項目は「何を分離するか（抽象）」と「実装・変更時に何をするか（テンプレート）」の対である。

## 使い方

- **新規画面**、**画面の契約やセクションを増やす変更**を始める前に、セクション番号順に頭とチェックリストを通す。
- **小さなバグ修正**でも、触る関心事に対応する行だけテンプレートを適用する。
- テンプレートのチェックが「不要」と断言できる場合のみスキップし、その理由をレビューで説明できるようにする。

---

## 0. エンドツーエンド順序（マクロのリマインダー）

| 順 | 関心事 | テンプレートの要約 |
|---|--------|-------------------|
| 1 | 事前定義 | §1 全体を埋める |
| 2 | 契約（型） | §2 で `UiState` / `Intent` / `Effect` / 表示モデルを固定 |
| 3 | ViewModel | §3 で更新源・I/O・effect 発行を分離 |
| 4 | 公開 Screen | §4 で購読・effect・nav のみ |
| 5 | Section UI | §5 で名前付き塊と引数設計 |
| 6 | リソース・プラットフォーム | §6 |
| 7 | Design system | §7（§6 `AGENTS.md` と併読） |
| 8 | a11y | §8 |
| 9 | Preview | §9 |
| 10 | テスト・検証可能単位 | §10 |
| 11 | モジュール・ファイル境界 | §11 |

---

## 1. 事前定義（実装より前）

### 1.1 ユーザー目的

- **抽象**: 画面がサービスする**単一のユーザー任務**（または明確な複数任務の列）を、実装の詳細なしで言語化する。ここがぶれると UI と VM に無秩序な分岐が付く。
- **テンプレート**:
  1. 「この画面でユーザーが終わらせること」を 1〜2 文で書く。
  2. 任務が複数なら、**タブ / フロー段階 / セクション**のどれで境界を切るか決める（無名の羅列にしない）。
  3. 画面外（前後の画面）との責務分界を一文で書く（何をここではしないか）。

### 1.2 セクションカタログ

- **抽象**: 認知上のブロックごとに**名前と責務**を先に固定し、以降の UI は必ずいずれかのセクションに帰属させる。
- **テンプレート**:
  1. セクション名一覧を作る（例: `Account`, `Notifications`, `DangerZone`）。
  2. 各セクションに「ユーザーがここで理解すること／できること」を 1 文ずつ書く。
  3. 新規コントロールは「どのセクションか」を決めてからコードを書く。

### 1.3 ナビゲーション出入り

- **抽象**: **ルート引数・戻り値・副作用（ログアウト後どこへ等）**は変更軸がナビと一体。曖昧だと Screen に条件が堆積する。
- **テンプレート**:
  1. 入り: 必須/任意の `NavKey` 引数、初期表示状態（例: どのダイアログを開くか）を列挙。
  2. 出り: 画面が発火しうる遷移（戻る、特定ルート、結果渡し）を列挙。
  3. 上記を `Intent` / `Effect` のどちらで表すか決める（遷移は通常 Effect 側で完結させ、Screen は購読して呼ぶだけに近づける）。

### 1.4 再入場・設定変更・プロセス死

- **抽象**: **状態の復元境界**（SavedStateHandle、再起動、設定変更）を決めないと、VM と UI で二重の「初期化」が生じる。
- **テンプレート**:
  1. 永続化が必要なフィールドと、再構成で捨ててよいフィールドを分ける。
  2. 単一の「ロード」エントリ（例: `init` ブロックまたは明示的 `load()`）に集約し、composable の `LaunchedEffect(Unit)` 乱立を避ける方針を決める。

---

## 2. 契約レイヤ（型・データフロー）

### 2.1 UiState

- **抽象**: 画面が描画に使う**読み取り専用の入力**全体。ビジネス不変条件はここに表現しすぎず、**表示に必要な形**に閉じる。
- **テンプレート**:
  1. `data class` または sealed 階層で、**ローディング / コンテンツ / エラー**など画面モードを表現できるようにする。
  2. フィールドは「この画面が読む」ものに限定。不要な domain 型をそのまま晒さない。
  3. 同じ意味の重複フィールド（例: `isLoading` と `state == Loading`）を作らない。

### 2.2 Intent（ユーザー操作）

- **抽象**: ユーザーが起こしうる**離散イベント**の列挙。コールバック地獄を Intent 名に寄せて平坦化する。
- **テンプレート**:
  1. `sealed interface` / enum で、ボタン・トグル・テキスト確定などを名前付きで列挙。
  2. パラメータは最小限（文字列は可能なら ID や value object ではなく、既に VM が解釈できる形）。
  3. Section の `onXxx` は `onIntent: (FooIntent) -> Unit` に揃えられるなら揃える。

### 2.3 Effect（一方向・消費一回）

- **抽象**: 状態に持たせない**一発の副作用**（ナビ、Toast、シートオープン）。繰り返し購読される State と混ぜない。
- **テンプレート**:
  1. `Channel` / `SharedFlow` 等、プロジェクト標準の effect パイプを使う。
  2. Effect の種類を sealed で列挙し、ペイロードを明示。
  3. Screen で `LaunchedEffect` 等により**消費**し、ナビゲータや `SnackbarHost` に渡す。VM 内で `Context` を使わない。

### 2.4 表示専用モデル（UiState 内のサブ型）

- **抽象**: 木の深い位置での `if` 連鎖をやめ、**列挙可能な表示パターン**に畳む。
- **テンプレート**:
  1. 3 パターン以上の見た目分岐は `sealed class RowUi` のような表示モデルに抽出する。
  2. 計算は `UiState` 生成時または pure `fun XxxUiState.toSections(): List<...>` に置く。
  3. Composable は `when (model)` で描画のみ行う。

### 2.5 失敗・空状態

- **抽象**: domain の失敗は**型**で上がり、**文言**は presentation で解決。文字列例外を契約に混ぜない（`AGENTS.md` §6 末尾）。
- **テンプレート**:
  1. `UiState` に `UserVisibleError` 相当（コード + オプション引数）または sealed エラー状態を持たせる。
  2. `stringResource(id, *args)` は composable または mapper（UI 層）に置く。
  3. 空リスト・権限なしなどを「エラー」と「空」で別表現にするか方針を決める。

---

## 3. ViewModel（単一更新源）

### 3.1 状態の単一集約

- **抽象**: **`MutableStateFlow` / `StateFlow`（または同等）一本**で画面状態を表す。composable 内 `remember { mutableStateOf }` でドメイン状態を持たない。
- **テンプレート**:
  1. 公開は `StateFlow<UiState>`（または `uiState` プロパティ）に限定。
  2. 更新は `copy` または `reduce` ヘルパーに集約。
  3. デバッグ用に状態遷移が追える粒度にする。

### 3.2 オーケストレーションとヘルパの分離

- **抽象**: 上位関数は**ステップの列挙**だけ。各ステップの詳細は named private function。
- **テンプレート**:
  1. `fun onIntent(i: Intent) = when (i) { ... }` は各分岐を 1 行の `handleXxx()` に委譲。
  2. `handleXxx()` 内も長ければ `startLoading` / `fetch` / `reduceSuccess` / `emitEffect` に分割。
  3. 同じ `Result` 処理パターンは `private fun` に抽出。

### 3.3 非同期 I/O

- **抽象**: リポジトリ呼び出しは**スコープとキャンセル**が明確な場所にのみ置く。
- **テンプレート**:
  1. `viewModelScope.launch` 内では `try`/`finally` で loading フラグを対称に戻す。
  2. 競合する連続呼び出しは `Job` キャンセルまたは最新のみ採用など方針を決めてから実装。
  3. メインスレッド規約（`Dispatchers`）はデータ層契約に従う。

### 3.4 Result・例外の扱い

- **抽象**: **成功と失敗の変換**を VM 内の一か所に寄せ、Composable には済んだ `UiState` だけ見せる。
- **テンプレート**:
  1. `map` / `mapCatching` / `fold` で domain → `UiState` 更新に変換。
  2. 想定外例外はクラッシュ回避とログ方針を決め、ユーザー向けには型付きエラーに落とす。
  3. 「再試行」「オフライン」など UI 操作に直結する失敗は `Intent` で表現可能にする。

### 3.5 ViewModel に置かないもの

- **抽象**: **Android UI 資源・Context・Navigation 具体型**は VM の依存から切り離す。
- **テンプレート**:
  1. `Context`, `Resources`, `@StringRes` 解決、NavController 直接保持を禁止リストに入れる。
  2. 必要ならインターフェース（例: `AuthTokenProvider`）は domain / data 側の抽象に置く。

---

## 4. 公開 Screen composable

### 4.1 責務の上限

- **抽象**: **購読・effect 消費・nav コールバック**が主業。レイアウトの「意味のある塊」は持たない。
- **テンプレート**:
  1. `val state by viewModel.uiState.collectAsStateWithLifecycle()`（またはプロジェクト標準）。
  2. `LaunchedEffect` は effect ストリームと key 管理に使う。ビジネスロジックを書かない。
  3. 本文は `FooScreenContent(state, onIntent)` または section 呼び出しのみ。

### 4.2 ViewModel 取得

- **抽象**: 依存解決と Factory は **DI（Koin）とモジュール**に閉じ、Screen は取得宣言だけにする（`AGENTS.md` §4.4、`docs/tech.md`）。
- **テンプレート**:
  1. 本プロジェクトでは `koinViewModel()`（または `koinViewModel(parameters = { parametersOf(...) })`）を標準とする。
  2. `ViewModel` のコンストラクタ引数は各 feature の `*FeatureModule` で `viewModel { ... }` に列挙し、composable 内に `object : ViewModelProvider.Factory` を書かない。
  3. AndroidViewModel が必要な場合も Koin の `viewModel { }` で `Application` を `get()` する。

### 4.3 Navigation 配線

- **抽象**: **ルート固有の引数の受け渡し**と、Effect からの遷移実行の境界を Screen に集約。
- **テンプレート**:
  1. `onNavigateXxx: () -> Unit` のようなコールバックを親から注入し、Effect ハンドラから呼ぶ。
  2. 深い section まで NavController を渡さない（Intent に閉じる）。

---

## 5. Section composable（feature 内）

### 5.1 引数設計

- **抽象**: **そのセクションが知るべき State の部分集合**と、**Intent へのゲートウェイ**だけを渡す。
- **テンプレート**:
  1. 引数は `(state: AccountSectionUi, onIntent: (Intent) -> Unit)` のように明示。
  2. 全域 `UiState` を渡す必要がなければ渡さない（再コンポーズ範囲の縮小）。
  3. コールバックは `onIntent(FooIntent.X)` に統一できるなら統一。

### 5.2 分岐の置き場所

- **抽象**: Section 内の `if` は**レイアウト分岐**に限定。ビジネス意味の分岐は上位で済ませる。
- **テンプレート**:
  1. `when (state.mode)` は 2〜3 分岐まで。超えるなら表示モデル（§2.4）へ戻す。
  2. 同じ条件が複数セクションで重複するなら `UiState` 側にフラグまたは型を持たせる。

### 5.3 スクロールコンテナ

- **抽象**: セクションが増える画面では **Lazy** でセクションを単位化し、巨大 `Column` を避ける。
- **テンプレート**:
  1. `LazyColumn` の `item { SectionA(...) }` 形式にする。
  2. `key` を安定 id で付与できるものは付与。

### 5.4 複雑な UI chrome（Pager 等）

- **抽象**: **画面全体の VM と独立した UI 状態**（選択タブ、アニメーション内部状態）は plain state holder に切り出す（`AGENTS.md` §4.4）。
- **テンプレート**:
  1. `rememberFooChromeState()` のような holder を別ファイルまたは同ファイル下部に置く。
  2. holder は `Intent` を代替しない。ドメイン操作は必ず VM へ。

---

## 6. リソース・プラットフォーム API

### 6.1 文字列・プルラル・フォーマット

- **抽象**: **文言とフォーマット**は Composable の composition ローカルまたは UI 層 mapper で解決。
- **テンプレート**:
  1. `stringResource(R.string.xxx)` を section composable 内で使う。
  2. VM には生の数値・ID だけ渡す。

### 6.2 Toast / Snackbar

- **抽象**: **表示要求**は Effect、**実際の表示**は Screen / Scaffold 階層。
- **テンプレート**:
  1. 共通 util（例: `showShortToast`）を使う場合も、呼び出しは UI 層に置く。
  2. メッセージ文言は UI 層で決定（Effect は event kind + 必要なら id）。

### 6.3 Dialog / BottomSheet

- **抽象**: **開閉状態**は UiState に含めてもよいが、**ナビ相当の結果**は Intent と Effect のどちらで扱うか一貫させる。
- **テンプレート**:
  1. 「確認してから削除」などは `UiState.dialog: ConfirmDelete?` のように nullable で表現。
  2. 結果は `Intent.ConfirmDelete` / `Intent.DismissDialog` のように Intent に落とす。

---

## 7. Design system（`:platform:designsystem`）

### 7.1 Token とプリミティブ

- **抽象**: **色・型・余白・形状**は atom/token 経由。feature で raw Material 色を直指定しない（`AGENTS.md` §4.4 / §6）。
- **テンプレート**:
  1. 新規の視覚属性が必要なら、まず DS に token があるか確認し、なければ DS 追加を検討してから画面で使う。
  2. 一時的な調整が「2画面目」で同じなら昇格（§7.3）。

### 7.2 Molecule の利用

- **抽象**: **複数 atom の定型組み合わせ**は molecule。画面位置の意味は feature（`AGENTS.md` §6）。
- **テンプレート**:
  1. `PasswordField` のような既存 molecule を優先。
  2. 画面専用の「この画面のカード」は feature の section に置く。

### 7.3 DS 昇格の判断

- **抽象**: **2 回目の同一パターン**で昇格候補。コピペ連鎖を事前に止める（`AGENTS.md` §4.2）。
- **テンプレート**:
  1. 2 画面で同じ slot 構成なら molecule 化を検討。
  2. 昇格時は slot ベース API（content lambda）を優先し、画面固有文言は slot に注入。

---

## 8. アクセシビリティ

### 8.1 ラベル・説明

- **抽象**: 意味のあるアイコン・画像・ボタンに**読み上げテキスト**を紐づける。
- **テンプレート**:
  1. アイコンのみボタンは `contentDescription` または `IconButton` の慣習に従う。
  2. 繰り返しリストは `Modifier.semantics { }` で必要に応じ collection 情報を付与。

### 8.2 タッチターゲット

- **抽象**: 操作可能要素は**推奨最小サイズ**を満たす。DS component が担うなら feature で縮めない。
- **テンプレート**:
  1. カスタムクリック領域は `minimumInteractiveComponentSize()` 等、プロジェクト標準を適用。

---

## 9. Preview

### 9.1 Section Preview

- **抽象**: **本物の VM を介さず**、表示モデルだけで見た目と状態差分を検証する。
- **テンプレート**:
  1. `@Preview` で `SectionX(state = fake, onIntent = {})`。
  2. 複数状態（loading / error / populated）で Preview を分ける。

### 9.2 Design system Preview

- **抽象**: DS public composable は **§6 `AGENTS.md`** に従い、同ファイルまたは近接ファイルに Preview を置く。

---

## 10. テストと検証可能単位

### 10.1 Pure mapper

- **抽象**: `DomainX -> UiState` / `Error -> UserMessageKey` は**副作用なし**に切り出し、単体テスト可能にする。
- **テンプレート**:
  1. `internal fun` または `object XxxUiMapper` に集約。
  2. 表形式の `ParameterizedTest` や単純 assert で網羅。

### 10.2 ViewModel テスト

- **抽象**: リポジトリを fake に差し替え、**Intent 列 → 最終 State / Effect** を検証する。
- **テンプレート**:
  1. `MainDispatcherRule` 等、プロジェクトの coroutine テストルールを使う。
  2. Effect は `take(1)` 等で消費検証。

---

## 11. モジュール・ファイル境界

### 11.1 置き場所

- **抽象**: **契約（UiState / Intent / Effect）**は presentation パッケージにまとめ、feature 外に漏らさない。
- **テンプレート**:
  1. `FooScreen.kt`（公開 + 薄い Content）、`FooViewModel.kt`、`FooUiState.kt`（または sealed 同一ファイル）のように責務で分割。
  2. Section が増えたら `settings/sections/AccountSection.kt` のようにディレクトリ分割。

### 11.2 依存方向

- **抽象**: feature presentation は **domain 公開 API と DS** に依存。data 実装に直接依存しない（Clean Architecture）。
- **テンプレート**:
  1. ViewModel のコンストラクタは interface（repository/use case）のみ。
  2. 違反が必要なら理由をレビューで説明し、短期の例外として明示。

---

## 付録: 「この変更はどの節か？」早見表

| 症状 | 見る節 |
|------|--------|
| 画面に条件が増えて読めない | 1.2, 2.4, 5.2 |
| VM が肥大化 | 3.2, 3.4 |
| Toast/Nav が散らばった | 2.3, 4.1, 6.2 |
| 文字列が VM にある | 2.5, 3.5, 6.1 |
| 同じカードをコピペ | 7.3, 1.2 |
| Preview が書けない | 2.4, 9.1 |
| タブ内部状態が VM と競合 | 5.4 |
