# `:platform:designsystem`（`platform/designsystem/`）

**Material 3 を唯一ここで直接利用**し、アプリ全体向けに **Atom / Molecule** を公開します。

## 規約（要約）

- **feature / app は `androidx.compose.material3` に依存しない**（コンベンションプラグインで検査）。
- 公開 Composable には **`@Preview`**（`NofyTheme` 使用）を付与。
- 画面固有の文脈は **feature に残す**（AGENTS.md の粒度規約）。

## ディレクトリ例

- `atom/` — ボタン、テキスト、テキストフィールド、アイコンなど
- `molecule/` — ダイアログ、複合バーなど
- `theme/` — `NofyTheme`、トークン

## 関連

- [`../README.md`](../README.md)
- [`../../AGENTS.md`](../../AGENTS.md)
- [`../../docs/AGENTS.md`](../../docs/AGENTS.md)（Material ポリシー詳細）
