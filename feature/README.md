# Feature モジュール

ユーザー向け機能を **api / impl** に分割したマルチモジュール構成です。

## 設計方針

- **`:feature:<name>:api`**: 他モジュールから参照してよい **ルート・契約・最小の公開 API** のみ。
- **`:feature:<name>:impl`**: Compose 画面、ViewModel、データ実装、Koin モジュール。
- パッケージは概ね次のレイヤーに分割します。
  - `presentation` — `screen`, `viewmodel`, `component`, `contract`（State / Intent・pending UI / navigation の consume）, `navigation`
  - `domain` — `model`, `repository`（抽象）, `usecase`, `error`
  - `data` — `local`, `repository`（具象）
  - `di` — Koin

## モジュール一覧

| Feature | Gradle | ディレクトリ | README |
|---------|--------|--------------|--------|
| Login | `:feature:login:api` / `:feature:login:impl` | `feature/login/api`, `feature/login/impl` | [login/README.md](login/README.md) |
| Note | `:feature:note:api` / `:feature:note:impl` | `feature/note/api`, `feature/note/impl` | [note/README.md](note/README.md) |
| Setting | `:feature:setting:api` / `:feature:setting:impl` | `feature/setting/api`, `feature/setting/impl` | [setting/README.md](setting/README.md) |

## UI 規約

- **Material 3 / `material3` への直接依存は禁止**（`:platform:designsystem` 経由のみ）。ビルド時 `verifyNoDirectMaterialUsage` で検査されます。
- 画面 composable は **薄く**保ち、セクション分割と state hoisting を優先（詳細は `AGENTS.md`）。ファイル分割の目安は [docs/source-layout.md](../docs/source-layout.md)。

## ビルド例

```bash
./gradlew :feature:login:impl:compileDebugKotlin
./gradlew :feature:note:impl:testDebugUnitTest
```
