# `shared/` — `:shared:*` ライブラリ

Gradle モジュール名とディレクトリ名は一致しています。

| Gradle 名 | ディレクトリ | README |
|-----------|--------------|--------|
| `:shared:auth` | `shared/auth/` | [auth/README.md](auth/README.md) |
| `:shared:settings` | `shared/settings/` | [settings/README.md](settings/README.md) |

インフラ寄りモジュールは [`../platform/README.md`](../platform/README.md)。親プロジェクト `:shared` の `projectDir` はこの `shared/` ルート（集約用 `build.gradle.kts` のみ）。
