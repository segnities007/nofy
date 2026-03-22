# ソース配置・ファイル分割

Kotlin / Android の公式方針と、このリポジトリ（`AGENTS.md`・`docs/tech.md`・`feature/README.md`）の約束をつなぐための実務ルールです。

## 公式の前提（要約）

- **Kotlin**: 原則としてファイル名は **公開トップレベル型の名前** と一致させる。関連が強い複数の宣言だけを **意図的に** 同一ファイルにまとめてよい（[Coding conventions / Source code organization](https://kotlinlang.org/docs/coding-conventions.html#source-code-organization)）。
- **Android UI 層**: 画面は **単方向データフロー**（`UiState` + イベント）を基本とし、状態と副作用の扱いを分離する（[Guide to app architecture — UI layer](https://developer.android.com/topic/architecture/ui-layer)）。

## このプロジェクトでの置き場所

| 種類 | 置き場所の目安 |
|------|----------------|
| 公開 screen composable | `feature/<name>/impl/.../presentation/screen/`（1 画面 1 ファイルが基本） |
| pending メッセージ・ナビの consume、`LaunchedEffect` などの UI 副作用オーケストレーション | 同パッケージの別ファイル（例: `*PendingUi.kt`、`*DialogHost.kt`）。screen は **購読・配線・セクション組み立て** に寄せる |
| `ViewModel` | `.../presentation/viewmodel/`。肥大化したら **責務ごと** に `*Reducer.kt`、`*Load.kt`、extension / internal helper ファイルへ分割（Kotlin に partial がないため） |
| `UseCase` / `Operation` | `docs/tech.md` の 3 層ルールに従う（domain に Android / Compose を持ち込まない） |
| design system の atom / molecule | `:platform:designsystem` のみ（feature から Material 直参照は禁止） |

## いつファイルを分けるか（目安）

次のいずれかに当てはまったら、**分割または抽出**を検討します（数値はチームで調整可）。

1. **公開 screen composable** が ~80 行を超え、または `Column` / `Box` / `when` のネストが 3 段を超え始めた → section composable または別ファイルの host / observer へ。
2. **`ViewModel` の 1 ファイル**が ~200 行を超えた、または `onIntent` / `launch` の分岐が読みにくい → `reduce*` / `execute*` を別ファイルの `internal` 関数や小さなクラスへ。
3. **同一ファイルに「無関係な」トップレベル型**が複数ある → Kotlin 慣習に従い、型ごとにファイルを分ける（例外は意図的な「関連宣言の同居」のみ）。
4. **Preview だけが肥大化**している → Preview 用データは `presentation/preview/` など既存の置き場へ集約（screen ファイルに長い fake を置かない）。

## 命名

- 画面: `FooScreen.kt` の `FooScreen`。
- 補助 composable: 役割が分かる名前（`FooScreenPendingUi.kt`、`FooEnrollmentDialogHost.kt` など）。
- `ViewModel`: `FooViewModel.kt`；切り出しは `FooViewModelPassword.kt` のように **ドメイン断面** で名付ける。

## Instrumented テストを追加するとき

現状 `src/androidTest` は置いていません。`androidTestImplementation`（JUnit / Espresso / Compose UI Test など）はモジュールの `build.gradle.kts` に **テスト追加時に** 入れ直してください。`app` で Compose の device テストを書く場合は `androidx.compose.ui:ui-test-junit4` と `debugImplementation` の `ui-test-manifest` が再び必要になります。

## 参照

- [readability.md](readability.md) — 早期リターン・宣言的 UI・公式リンク集
- [docs/tech.md](tech.md) — `ViewModel` / `UseCase` / `Operation` の境界
- [../feature/README.md](../feature/README.md) — feature 内パッケージ（presentation / domain / data / di）
- [../AGENTS.md](../AGENTS.md) — Compose のネスト抑制・state hoisting
