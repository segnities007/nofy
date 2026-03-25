# Feature: Note（`:feature:note`）

暗号化されたノートの **一覧・編集・ページング** を扱う機能です。

## Gradle

- **`:feature:note:api`** — ルート等の公開 API（`feature/note/api/`）。
- **`:feature:note:impl`** — UI・ViewModel・Room・Repository 実装（`feature/note/impl/`）。

## データ層

| 要素 | 説明 |
|------|------|
| `NoteDatabase` | Room + **SQLCipher**（`SupportOpenHelperFactory`） |
| `NoteRepositoryImpl` | フィールド暗号化（`DataCipher`）とガード付き CRUD |
| スキーマ | `exportSchema = true`、JSON は `feature/note/impl/schemas/` に出力（KSP `room.schemaLocation`） |

## プレゼンテーション

- **複数ページ**（空白トレーリングページ、状態遷移）は `presentation.state` に集約。
- ユニットテストは **`presentation/state` パッケージとディレクトリを一致**（Kotlin 規約）。

## セキュリティ

- ノート読み書きは `SensitiveOperationGuard` 経由。危険環境検知時は fail-closed。

## 関連 README

- [api/README.md](api/README.md)
- [feature/README.md](../README.md)
