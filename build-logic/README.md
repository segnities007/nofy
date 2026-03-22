# `build-logic` — Convention plugins

Gradle **includeBuild** として読み込まれるローカルプラグイン群です。アプリ本体のモジュールから `plugins { id("nofy.android.*") }` のように参照されます。

## 含まれるもの

| プラグイン（例） | 役割 |
|------------------|------|
| `nofy.android.library` | Android ライブラリの共通設定 |
| `nofy.android.feature` | Feature 用: Compose 有効、Compose BOM、**Material 直import 禁止タスク** |

## 開発時の注意

- ここを変更すると **全モジュールのビルド挙動**に影響します。
- 変更後は `./gradlew :app:assembleDebug` と主要 feature の `compileDebugKotlin` を確認してください。

## 関連

- ルート [`README.md`](../README.md)
- バージョンカタログ [`../gradle/libs.versions.toml`](../gradle/libs.versions.toml)
