# `platform/` — `:platform:*` ライブラリ

Gradle モジュール名とディレクトリ名は一致しています（`settings.gradle.kts` の `projectDir`）。

| Gradle 名 | ディレクトリ | README |
|-----------|--------------|--------|
| `:platform:navigation` | `platform/navigation/` | [navigation/README.md](navigation/README.md) |
| `:platform:database` | `platform/database/` | [database/README.md](database/README.md) |
| `:platform:storage` | `platform/storage/` | [storage/README.md](storage/README.md) |
| `:platform:crypto` | `platform/crypto/` | [crypto/README.md](crypto/README.md) |
| `:platform:biometric` | `platform/biometric/` | [biometric/README.md](biometric/README.md) |
| `:platform:designsystem` | `platform/designsystem/` | [designsystem/README.md](designsystem/README.md) |

親プロジェクト `:platform` の `projectDir` はこの `platform/` ルート（集約用 `build.gradle.kts` のみ）。共有モジュールは [`../shared/README.md`](../shared/README.md)。
