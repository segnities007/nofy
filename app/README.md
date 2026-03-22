# `:app` — Application シェル

アプリのエントリポイントと、**グローバルな横断関心**だけを担当する薄いモジュールです。

## 責務

| 領域 | 内容 |
|------|------|
| **起動** | `NofyApplication` で Koin 初期化、`MainActivity` で Compose ルート |
| **ナビゲーション統合** | `NofyNavHost`、各 feature の `NavigationEntryInstaller` を Koin から収集 |
| **セキュリティ（環境）** | `RiskyEnvironmentDetector`、スナップショット `RiskyEnvironmentSnapshotHolder`、`SnapshotSensitiveOperationGuard` の DI 登録 |
| **ウィンドウポリシー** | `FLAG_SECURE`、オーバーレイ抑制、マルチタスクスナップショット無効化（対応 API） |
| **ライフサイクル** | バックグラウンド時のロック、`RiskyEnvironment` ポーリング、アイドルタイムアウトロック |

## 依存関係（概要）

- 全 feature の **api + impl**
- `platform:*`（designsystem, navigation, crypto, database, storage, biometric）
- `shared:auth`, `shared:settings`
- Koin、Navigation 3、Compose

## 関連ドキュメント

- ルート [`README.md`](../README.md)
- 技術詳細 [`docs/tech.md`](../docs/tech.md)
- エージェント規約 [`AGENTS.md`](../AGENTS.md)

## ビルド

```bash
./gradlew :app:assembleDebug
```
