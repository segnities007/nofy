# 可読性・宣言的コードの指針

Go の **ガード節（異常・無効条件を先に処理して `return`）** と、Android / Compose の **宣言的 UI・単方向データフロー** を、このリポジトリでどう扱うかの整理です。数値目安は [source-layout.md](source-layout.md) と [AGENTS.md](../AGENTS.md) と合わせて読んでください。

## 1. 早期リターン（ガード節）

**考え方**: 無効入力・失敗・「何もしない」条件を **関数の先頭で処理して抜ける**。正常系は **左に寄ったまま下に続く**（ネストした `if-else` を避ける）。

- **Kotlin**: 制御フローは [Conditions and loops](https://kotlinlang.org/docs/control-flow.html) に従う。`return` / `return@label` で意図を明示する。
- **Android Kotlin スタイル**: [Kotlin style guide](https://developer.android.com/kotlin/style-guide) で一貫した書き方を保つ。
- **本リポジトリ**: ルート [AGENTS.md](../AGENTS.md) に「guard clause でネストを潰す」「Go の early return と同じ発想」と明記済み。

**向いている場所**

- `ViewModel` の `suspend` リデューサ（**失敗を先に**、成功は最後）。
- `UseCase` / repository 結果の分岐（`if (result.isFailure) { ...; return }`）。
- 純粋関数のバリデーション（`if (x.isBlank()) return null`）。

**Compose における注意**

- `@Composable` 本体の **途中で `return`** は、公式が許容するパターン（例: `if (!feature) return`）に限り、濫用しない。
- ツリーが深くなるくらいなら **別 composable に切る**ほうが「宣言的」（下記）。

## 2. 宣言的 UI（Compose）と単方向データフロー

**宣言的**: 「今の状態 `state` なら UI はこう描く」と **状態を引数にした関数**として書く。子は state を直接書き換えず、**イベントを親へ送る**。

- [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state)
- [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting)
- [UI layer / UDF](https://developer.android.com/topic/architecture/ui-layer)（状態は下へ、イベントは上へ）

**読みやすさのコツ**

- 公開 screen は **短いオーケストレーション**に留め、`LaunchedEffect`・ダイアログ・セクションは **名前付き composable** に分離する（「画面 = 副作用 + 骨格 + オーバーレイ」の並びが見えるようにする）。
- 深い `if` / `when` は **ツリーの外**で状態や `sealed` に畳んでから描画する（[AGENTS.md](../AGENTS.md) の「分岐は UI tree の外で解決」）。

## 3. 命令的処理の隔離（「宣言的に近い」オーケストレーション）

`ViewModel` の上位関数は **やることの列挙**だけにし、callback や `launch` の **中身を長くしない**（[AGENTS.md](../AGENTS.md)・[tech.md](tech.md) の Operation / named helper）。

- 成功時の変換は `Result.map` / 純関数へ寄せる。
- Biometric など手続きが長いものは `suspend` の **専用関数**に閉じ、内部でガード節を使う。

## 4. チェックリスト（レビュー用）

- [ ] 異常系・空入力は **先に return** しているか（成功パスが一段左にまとまっているか）。
- [ ] `suspend` の **finally**（または `use`）で必ず解放すべきリソース・フラグがあるか（例: `setBusy(false)`）。
- [ ] Compose で **ネストが 3 段**を超えていないか（section / host に分割したか）。
- [ ] `sealed` の `when` は **網羅**されているか（早期 return と両立できる）。

## 参照（一次・準一次）

| トピック | リンク |
|----------|--------|
| Kotlin coding conventions | [kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html) |
| Kotlin 制御フロー | [kotlinlang.org/docs/control-flow.html](https://kotlinlang.org/docs/control-flow.html) |
| Android Kotlin style | [developer.android.com/kotlin/style-guide](https://developer.android.com/kotlin/style-guide) |
| Compose state | [developer.android.com/develop/ui/compose/state](https://developer.android.com/develop/ui/compose/state) |
| State hoisting | [developer.android.com/develop/ui/compose/state-hoisting](https://developer.android.com/develop/ui/compose/state-hoisting) |
| App architecture / UI layer | [developer.android.com/topic/architecture/ui-layer](https://developer.android.com/topic/architecture/ui-layer) |
