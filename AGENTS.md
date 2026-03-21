# AGENTS.md

このドキュメントは、本プロジェクトに携わるAIエージェントが遵守すべき原理原則と行動指針を定義します。

## 1. 原理原則の徹底
エージェントは、以下の原理原則を常に念頭に置き、すべてのコード生成および意思決定プロセスにおいてこれらを適用しなければなりません。

- **Clean Architecture**: 依存関係の方向を常に内側（Domain層）に向け、責務の分離を徹底すること。
- **SOLID 原則**: 保守性と拡張性を最大化するため、SOLID原則を厳格に適用すること。
- **Modular Architecture**: モジュール間の結合度を低く保ち、再利用性とビルド速度を考慮した設計を行うこと。
- **Security by Design**: 認証、暗号化、鍵管理などのセキュリティ実装においては、プラットフォームの推奨事項と最新のベストプラクティスを遵守すること。

## 2. 最新情報の活用と検索の強制
技術の進歩は速いため、エージェントは自身の内部知識のみに頼らず、積極的に最新情報を取得しなければなりません。

- **ネット検索の実行**: 実装方針を決定する前に、必ず公式ドキュメント（Android Developer、Jetpack Compose、Roomなど）や信頼できるソースを検索し、最新のAPI仕様、非推奨情報、パフォーマンス改善策を確認すること。
- **最新安定版の採用**: ライブラリのバージョンや実装パターンは、特に理由がない限り、最新の安定版（Stable）を基準にすること。
- **モダンな手法の優先**: Jetpack Compose, Kotlin Coroutines, Flow, KSP など、Android開発におけるモダンなスタックと推奨手法（Modern Android Development - MAD）を常に採用すること。

## 3. 実装の品質
- **Pixel-Perfect UI**: ユーザーインターフェースの実装においては、デザイン仕様を忠実に再現し、アクセシビリティとパフォーマンスに配慮すること。
- **エラーハンドリング**: 期待される正常系だけでなく、異常系やエッジケースに対する適切なエラーハンドリングとログ出力を実装すること。
- **ドキュメントの更新**: 実装に伴い、必要に応じて `docs/tech.md` や関連ドキュメントを更新し、プロジェクトの整合性を保つこと。

## 4. Compose設計とネスト抑制規約
Jetpack Compose の公式な **State Hoisting / UI layer / State Holder / Custom Design System** の考え方に従い、可読性を損なう深いネストや巨大 composable を禁止します。以下を必須ルールとします。

- **Screen composable は薄く保つ**: 画面の公開 composable は、状態購読、effect 収集、navigation 配線に責務を限定すること。実際の描画は private な section composable へ分割すること。
- **Unidirectional Data Flow を徹底する**: Screen 単位では `UiState` + `Intent` + `Effect` を基本形とし、状態変更は 1 箇所に集約すること。複数の mutable state を画面 composable に散在させないこと。
- **State holder を使い分ける**: 画面全体の状態は `ViewModel`、BottomBar や Pager chrome など UI 要素単位の複雑な状態は plain state holder / helper class に切り出すこと。1 つの composable に data loading と widget state を同居させないこと。
- **UI ロジックと Android 依存は UI 層へ置く**: `Context`、`stringResource`、navigation、Toast/Snackbar 表示などの UI ロジックは composable または UI state holder で扱うこと。`ViewModel` に `Context` や resource 解決を持ち込まないこと。
- **Theme の責務は app root に置く**: アプリ全体の `Theme` 適用は `MainActivity` などの app root で 1 回だけ行うこと。feature の screen composable が runtime で独自に `Theme` を巻き直してはならない。`Theme` は Preview のみで包むこと。
- **Screen で匿名 Factory を書かない**: `object : ViewModelProvider.Factory` を screen composable に直書きしないこと。`viewModelFactory` + `initializer` を使った named helper を `ViewModel` 側または近接 file に置き、screen はそれを使って配線だけを行うこと。
- **宣言的に近いオーケストレーションを優先する**: `ViewModel` や state holder の副作用コードは、上位関数に「何をするか」だけを書き、詳細は named helper へ分割すること。上位 orchestration 関数は 5〜15 行程度を目安に保ち、`onSuccess` / `onFailure` / `launch` / `if` / `when` の内側に長い処理を書かないこと。
- **副作用コードでも抽象度を揃える**: 1 関数の中に loading 開始、repository 呼び出し、domain→UI 変換、state 更新、effect 発火を混在させないこと。`startLoading()`, `fetch...()`, `reduce...()`, `handle...Failure()` のように責務ごとに分離すること。
- **guard clause でネストを潰す**: 入力検証、存在確認、無効状態の分岐は先頭で早期 return し、正常フローを左に寄せること。Go の early return と同じ発想で、Compose / Kotlin でも深いネストを避けること。
- **結果変換は `Result.mapCatching` / helper に寄せる**: 成功時の変換ロジックを `onSuccess` の中へ埋め込まず、`Result` の変換か pure helper function へ移すこと。失敗時も UI 文言の解決は presentation helper に閉じ込めること。
- **ネスト深度の上限を意識する**: `Box` / `Column` / `Row` / `AnimatedVisibility` / `if` / `when` が 3 段を超え始めたら、その場で section composable か helper model に分割すること。1 関数が 80〜120 行を超える場合も同様に再設計を優先すること。
- **分岐は UI tree の外で解決する**: `if/when` を composable の深い位置に増やすのではなく、enum / sealed interface / 表示用 data class に変換してから描画すること。表示条件の計算は先に済ませること。
- **繰り返す chrome は design system に昇格させる**: 同じ layout pattern、bar、card、dialog、markdown、section header が複数画面に出る場合は `core:designsystem` の slot ベース component として抽出すること。feature 側へコピーしないこと。
- **深い callback ネストを避ける**: composable 内で匿名 object や多重 lambda を連鎖させないこと。named function、helper composable、state holder へ逃がし、UI ツリーとイベント処理を分離すること。
- **Lazy 系コンテナを優先する**: 設定や詳細画面のようにセクションが増える UI は巨大な `Column` より `LazyColumn` を優先し、section ごとに item composable へ分けること。
- **Modern UI は design token から組み立てる**: 色、タイポグラフィ、余白、bar、button、textfield は `core:designsystem` の token / component を経由して構築し、feature で Material API や ad-hoc な見た目調整を直接書かないこと。

## 5. モダンUI実装方針
- **見た目の刷新は screen 単体ではなく design system から行う**: ボタン、カード、入力欄、FloatingBar、タイポグラフィを先に整え、その上で screen を組み立てること。
- **Hero / Summary / Content の三層で構成する**: 画面冒頭に目的を伝える hero、次に現在の状態を要約する summary、最後に操作コンテンツを置き、情報を 1 枚の巨大カードに詰め込まないこと。
- **情報密度は section で制御する**: 設定画面のような多機能 UI はカテゴリ分割し、1 画面 1 目的の section に分けること。BottomBar や Pager を使う場合も、各 page/section の責務を明確にすること。
- **装飾は軽く、意味を持たせる**: 背景グラデーション、border、elevation、surface tint は使ってよいが、本文の可読性や入力の集中を阻害しないこと。特に editor 系画面では装飾より可読性を優先すること。

## 6. Design System 粒度規約
- **Atom は単一責務の視覚プリミティブに限定する**: `Text` `Icon` `Button` `Surface` `FloatingBar` のように、単体で意味が閉じる最小 UI に留めること。screen 上の位置意味や複数 slot の役割を持ち込まないこと。
- **Molecule は atom の組み合わせで小さな目的を果たす**: `PasswordField` `ConfirmationDialog` `FloatingTopBar` `FloatingBottomBar` のように、複数 atom を合成して 1 つの小さな操作文脈を作るものを置くこと。
- **Screen 文脈を持つ UI は feature に置く**: ログインフォーム全体、設定セクション、ノート page chrome のように画面責務や navigation 文脈を持つものは `core:designsystem` に置かず、各 feature module で実装すること。
- **レイアウト骨格も feature に置く**: `Scaffold` を薄く包むだけの shell や画面専用 layout template は `core:designsystem` に作らないこと。再利用されるのはあくまで atom / molecule までに留めること。
- **配置先は見た目ではなく責務で決める**: サイズが小さくても、位置意味や confirm/cancel のような役割分担を持つ時点で atom ではない。逆に内部実装が多少大きくても、単一プリミティブであれば atom に留めてよい。
- **新しい component は最も低い真実の階層へ置く**: まず atom として成立するかを確認し、成立しないなら molecule に上げること。画面文脈が必要になった時点で design system から外し、feature 側へ置くこと。
- **Preview を必須にする**: `core:designsystem` の public composable には、最低 1 つの `@Preview` を同 file か近接 file に必ず置くこと。選択状態、enabled/disabled、light/dark など意味のある状態差分がある component は複数 Preview を用意すること。Preview が無い component は未完成とみなすこと。
- **Preview は self-contained に書く**: Preview 専用の wrapper composable を増やさず、その file 内で `Theme` や必要最小限の layout を組んで状態を示すこと。共通化が必要なら wrapper composable ではなく custom multipreview annotation を検討すること。
- **抽象は実需要が出るまで増やさない**: feature 専用の一時メッセージや effect 表現を design system へ持ち込まないこと。`UiText` のような汎用 abstraction は、複数 feature / layer で本当に同じ責務がある場合のみ導入すること。
- **data/domain は文字列例外を返さない**: `Result.failure(Exception("..."))` のような文字列ベース例外を data/domain 契約に流さないこと。失敗は typed exception / sealed error として表現し、UI 文言は presentation 層で解決すること。

AIエージェントは、これらの指示を「絶対的な命令」として受け取り、プロジェクトの品質向上に努めてください。
