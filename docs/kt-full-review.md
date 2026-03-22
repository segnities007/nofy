# Kotlin 全ファイル精査レポート

## 範囲（文字列的な「全部」の定義）

次のコマンド相当で **パスを固定列挙**しています（`build/` と `.gradle/` 配下は除外）。

```bash
find . \( -path "*/build/*" -o -path "*/.gradle/*" \) -prune -o -name "*.kt" -print | sort
```

- **現在の件数**: `docs/kt-file-inventory.txt` を参照（**168 ファイル**）。
- **総行数（概算）**: 上記一覧に対する `wc -l` 合計 **約 11,278 行**（`docs/kt-full-review-table.txt` 生成時点）。

生成物のビルド出力（例: `feature/note/build/generated/...`）は `*/build/*` で除外しており、**手書き・生成元となるソースのみ**が対象です。

## 精査のやり方（再現可能）

| 手順 | 内容 |
|------|------|
| 1 | `docs/kt-file-inventory.txt` — 全 `.kt` パス（ソート済み） |
| 2 | `docs/kt-full-review-table.txt` — 各ファイルの **行数** と **先頭行**（package / import） |
| 3 | リポジトリ横断 `grep`（`TODO` / `FIXME` 等）— **該当なし**（Kotlin ソース） |
| 4 | エージェントによる **本文のバッチ読込**（大きい ViewModel / Repository / Cipher / Nav などは全行、短い contract・route は全行） |
| 5 | `platform/designsystem` の atom/molecule は **同型パターン**（トークン経由の薄いラッパ）が大半で、個別に「問題あり」とは判断していない |

**限界**: 人間が紙面上で 1 行ずつ声に出して読むのと同じ検証は、ツール上では代替できません。本レポートは **全パスを漏れなく列挙し、メトリクスと読込を組み合わせた監査**です。

## 今回の精査で実施した修正

| 項目 | 内容 |
|------|------|
| **誤コミットされたビルド成果物** | `build-logic/convention/bin/main/*.kt` は `src/main/kotlin` の **古い複製**（`AndroidFeatureConventionPlugin` に検証タスク等が欠落）。**ファイルを削除**し、`.gitignore` に `build-logic/convention/bin/` を追加。 |
| **インベントリ更新** | 上記削除後、`kt-file-inventory.txt` を **168 件**に更新。 |

`./gradlew :build-logic:convention:compileKotlin :app:compileDebugKotlin` は成功を確認済み。

## モジュール別サマリ（168 ファイル全体の所感）

- **app**: Application / Activity / Nav / セキュリティ画面・検出器・テスト。Nav のルート解決は `authGateRouteOrNull` で DRY 化済み。
- **build-logic/convention**: **ソースは `src/main/kotlin` のみ**が正。`bin/` は無視。
- **feature:login|note|setting（api+impl+test）**: Contract / ViewModel / Operation / Screen の分離は一貫。login の `reduce*` 重複は任意で共通化可。
- **platform:**: biometric / crypto / database / designsystem / navigation / storage。暗号・DB は変更コスト大。designsystem はコンポーネント数が多いが各ファイルは小〜中規模。
- **shared:auth|settings**: ドメインとデータ実装の境界は明確。`AuthRepositoryImpl` は長いがヘルパ分割済み。

## 付録: 行数・先頭行一覧

機械生成の完全表は **`docs/kt-full-review-table.txt`**（Markdown 表形式、`|` 区切り）。

フルパス一覧は **`docs/kt-file-inventory.txt`**。

## 関連ドキュメント

- [codebase-refactoring-audit.md](codebase-refactoring-audit.md) — リファクタ候補の優先度メモ
- [readability.md](readability.md) — 可読性・早期リターン
