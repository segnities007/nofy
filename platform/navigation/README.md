# `:platform:navigation`（`platform/navigation/`）

アプリ全体の **ナビゲーション拡張**（例: feature がルートを登録する `NavigationEntryInstaller`）を置きます。

## 方針

- 具体的な画面 Composable は **各 feature** が保持。
- 本モジュールは **配線のための最小の契約・ユーティリティ** に留める。

## 関連

- [`../README.md`](../README.md)
- `app` の `NofyNavHost`
