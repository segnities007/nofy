# `:platform:crypto`（`platform/crypto/`）

ノート本文の **セッション鍵**、**Argon2 パスワードハッシュ**、**Keystore 連携**（AES / HMAC / 生体用鍵ファクトリ）を担当します。

## 主な型

| 型 | 役割 |
|----|------|
| `DataCipher` | パスワード束縛のセッション鍵、ロック、レガシー移行 |
| `PasswordHasher` | Argon2id + pepper（Keystore HMAC） |
| `PasswordBoundSessionKeyProtector` | ラップされたセッション状態 |
| `BiometricCipher` | 生体必須・enrollment 無効化に追従する AES 鍵 |

## セキュリティ

- 平文パスワードの `ByteArray` は処理後 **ゼロフィル**する方針です。

## 関連

- [`../auth/README.md`](../auth/README.md)
- [`../biometric/README.md`](../biometric/README.md)
