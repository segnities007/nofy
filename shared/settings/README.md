# `:shared:settings`（`shared/settings/`）

アプリ外観に関する **UI 設定**（テーマ、フォント倍率、フォアグラウンド無操作ロック時間など）の契約と、永続化のためのリポジトリ抽象を提供します。

## 責務

- 設定の **読み書き API**（Flow など）を feature / app が利用。
- 実体の暗号化ストアは `platform:storage`（datastore モジュール）側と連携。

## 関連

- [`../datastore/README.md`](../datastore/README.md)
- [`../README.md`](../README.md)
