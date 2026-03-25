# `:platform:storage`（`platform/storage/`）

**Keystore で保護した AES-GCM** により、SharedPreferences 上の値を暗号化して保存するストアです。

## 責務

- `KeystorePreferencesStore` — 論理キーをハッシュし、値を IV + 暗号文として保存。
- `AuthLocalDataSource` / 設定用データソース — 認証メタデータや UI 設定の永続化。

Gradle モジュール名は **`:platform:storage`** です（ディレクトリ名は `datastore`）。

## 関連

- [`../settings/README.md`](../settings/README.md)
- [`../auth/README.md`](../auth/README.md)
