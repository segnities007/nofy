# コードベース調査: リファクタ候補（手動スキャン）

`*.kt`（`build/` 除外）の **全パス列挙・行数表** は [kt-full-review.md](kt-full-review.md) / [kt-file-inventory.txt](kt-file-inventory.txt) を参照（現在 **168 ファイル**）。本稿はその上での **リファクタ候補** メモです。優先度は「保守頻度 × 複雑さ × リスク」の主観です。

## 行数が大きいファイル（要所）

| 行数目安 | パス | 所見 |
|----------|------|------|
| ~380 | `feature/note/.../NoteViewModel.kt` | 責務は既に小関数に分割済み。リデューサは **失敗ファースト** に揃え済み（本調査で反映）。 |
| ~319 | `shared/auth/.../AuthRepositoryImpl.kt` | `runCatching` + 小さな `ensure*` で読みやすい。分割するなら **パスワード試行ロックアウト**系を別ファイルの `internal` オブジェクトへ。 |
| ~304 | `platform/crypto/.../DataCipher.kt` | 暗号処理のため触るコストが高い。`decrypt` の `fold` は読みにくいが、挙動変更リスク大。**テストを伴わない整形は非推奨**。 |
| ~286 | `feature/setting/.../SettingViewModel.kt` | 設定系と同様に整理済み。 |
| ~257 | `app/.../RiskyEnvironmentDetector.kt` | `detect()` 内の `buildList` は宣言的。分割するなら **検査関数をカテゴリ別ファイル**（debug / root / hook）へ。 |
| ~242 | `feature/login/.../RegisterViewModel.kt` | 生体登録フローが縦に長い。`continueBiometricEnrollment` チェーンは **1 ステップ = 1 suspend 関数**のまま維持で十分。 |
| ~232 | `feature/login/.../LoginViewModel.kt` | `unlock` を **空パスワード先判定**に整理済み。`reducePasswordUnlock` / `reduceBiometricUnlock` の `when` は重複 → **private fun reduceSubmissionResult** 共通化が可能（任意）。 |
| ~208 | `app/.../navigation/NofyNavHost.kt` | `resolveInitialRoute` / `resolveForcedRoute` の共通条件は **`authGateRouteOrNull` に集約済み**。`PreviewAuthRepository` の肥大は任意で別ファイル化可。 |
| ~189 | `feature/note/.../NoteScaffold.kt` | `SettingsScaffold` と同様、**TopBar / BottomBar / Scrims** を private composable に切るとメインが短くなる（任意）。 |

## モジュール別メモ

### `app`

- **MainActivity**: 既に責務が限定的。大きな分割は不要。
- **NofyNavHost**: ルート解決の重複は **ガード節ヘルパ**で解消済み。

### `feature:login`

- **Operation 群**: ファイルサイズは小さめ。命名と責務は一貫している。
- **LoginViewModel**: 上記 `reduce*` 共通化が唯一の目立つ重複。

### `feature:note`

- **NoteScreen**: pending 処理は `SettingsScreenPendingUi` と同パターンに揃え済み。
- **NoteRepositoryImpl** / **NoteBody** / **NoteEditorPage**: 現状サイズは許容。エディタ周りが今後伸びたら **preview / edit を composable 分割**。

### `feature:setting`

- 直近で screen / scaffold / viewmodel を整理済み。**追加の緊急課題なし**。

### `shared:auth`

- **AuthRepositoryImpl**: セキュリティクリティカル。リファクタは **挙動同等のテスト**を前提に。

### `platform:*`

- **BiometricAuthenticator**, **KeystorePreferencesStore**: I/O と API 境界が混ざるが、ファイルサイズは中程度。
- **designsystem**: atom/molecule 単位で既に細かい。

## 横断的な改善アイデア（任意）

1. **Toast 表示**: `NoteScreen` と `SettingsScreenPendingUi` で `showToast(context, res)` が重複。共通化するなら **`platform:designsystem` ではなく** `shared` または小さな `presentation/util`（Android 依存を閉じる）を検討。いまは **feature 内重複許容**でもよい。
2. **pending + consume パターン**: login / note / setting で同型に近い。**ドキュメント**（[readability.md](readability.md)）でパターン化済みならコード共通化は必須ではない。
3. **Result リデューサ**: `isFailure` 先に処理するスタイルを ViewModel 間で統一（setting / note で揃えた）。

## 今回の調査で実施した変更

- `NoteViewModel`: `reduceNotesLoad` / `reducePageSave` / `reduceDeletion` / `reduceLock` を **失敗・異常を先に処理**する形へ。
- `LoginViewModel`: `unlock` を **空パスワードのガード節**へ。
- `NoteScreen`: pending `LaunchedEffect` を **null 早期 return + when** に統一、未使用 import 削除。
- `NofyNavHost`: 認証ゲート用ルート解決を **`authGateRouteOrNull`** に DRY 化（`AuthNavigationRouteTest` 維持）。

## 参照

- [readability.md](readability.md) — 早期リターン・宣言的 UI
- [source-layout.md](source-layout.md) — ファイル分割の目安
