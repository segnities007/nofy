# Feature: Setting（`:feature:setting`）

テーマ・フォント倍率・セキュリティ関連設定・OSS ライセンス表示など **アプリ設定** を扱います。

## Gradle

- **`:feature:setting:api`** — `feature/setting/api/`
- **`:feature:setting:impl`** — `feature/setting/impl/`

## 主な責務（impl）

- UI 設定の読み書き（`shared:settings` / Keystore バックのストレージと連携）
- 生体認証の **登録フロー**（設定画面からの `BiometricCipher` 利用）
- アプリリセット等の破壊的操作（ドメイン・ガードと整合）

## 命名について

コードベース内で `Setting` と `Settings` が混在する箇所があります。新規コードでは **「画面・Intent・State は単数 Setting」「ルートやセクション集合は Settings」** のように、役割で揃えると読みやすくなります（既存の大規模リネームは別タスク推奨）。

## 関連 README

- [api/README.md](api/README.md)
- [feature/README.md](../README.md)
