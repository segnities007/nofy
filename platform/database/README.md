# `:platform:database`（`platform/database/`）

暗号化 Room DB の **ライフサイクル**（アンロック、ロック、パスフレーズ変更、削除）を抽象化する薄い契約層です。

## 責務

- `SecureDatabaseController` インターフェース（実装は note feature の `NoteDatabaseProvider` など）。

## 関連

- 実 DB 定義: `feature/note/impl` の `NoteDatabase`
- [`../README.md`](../README.md)
