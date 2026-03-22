# `:shared:auth`（`shared/auth/`）

認証ドメイン（パスワードポリシー、ロックアウト、リポジトリ契約、**機微操作ガード**のインターフェース）をまとめます。

## 責務

- **ドメイン**: `AuthRepository` 契約、`PasswordPolicy`、`SensitiveOperationGuard` インターフェースなど。
- **データ実装**: `AuthRepositoryImpl`（パスワードハッシュ、DB アンロック、生体シークレットのメタと連携）。
- 実装は `platform:database` / `platform:crypto` / `platform:storage`（datastore）に依存。

## 注意

- **`SensitiveOperationGuard` の具体実装**（スナップショット参照）は `:app` で登録します。shared は契約のみを知ります。

## 関連

- [`../README.md`](../README.md)
- [`../crypto/README.md`](../crypto/README.md)（`PasswordHasher`）
