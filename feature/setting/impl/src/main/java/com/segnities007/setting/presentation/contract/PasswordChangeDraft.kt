package com.segnities007.setting.presentation.contract

/**
 * 設定画面「パスワード変更」フォームの下書き（平文）。
 *
 * ## プロジェクト規約との関係
 * [AGENTS.md] の §4 および [docs/compose-screen-micro-template.md] は、原則として画面状態を
 * `ViewModel` の `UiState`（単一更新源）に寄せる。本型はその例外として、**平文パスワード下書きだけは
 * composition スコープの `remember` 上に保持する**（`rememberSettingsPasswordDraftHolder`、
 * `presentation.screen` パッケージ）。
 *
 * **理由（セキュリティ）**
 * - `StateFlow` に載せると、購読・デバッグ・将来の誤ったシリアライズなどで**触れやすい表面積**が増える。
 * - `rememberSaveable` は Saved Instance State に載りうるため、**プロセス再生成時にバンドル経由で残る**リスクがあり、
 *   平文パスワードには使わない。
 *
 * **「ViewModel より寿命が短い／長い」について**
 * - **設定画面に留まる限り**、構成変更（回転）では `ViewModel` も `remember` も通常どちらも生き残るため、
 *   **「VM の方が常に長生き」とは言えない**（同じ画面では同程度）。
 * - **差分**は主に、(1) 下書きを **Saved State / Flow 観測の対象にしない**こと、(2) 画面を離れて `ViewModel` が破棄される場合は
 *   composition も破棄されやすく、**保持期間が用途に閉じやすい**こと。
 * - プロセスキル後は、`SavedStateHandle` を使わない限り VM も `remember` も再生成される；平文をバンドルに載せない限り、
 *   意図しない「ディスク付近への退避」は避けられる。
 *
 * 参照: リポジトリルート `AGENTS.md` §4、`docs/compose-screen-micro-template.md`
 */
data class PasswordChangeDraft(
    val current: String = "",
    val new: String = "",
    val confirm: String = "",
) {
    fun canSubmit(isPasswordUpdating: Boolean): Boolean {
        if (isPasswordUpdating) return false
        if (current.isBlank()) return false
        if (new.isBlank()) return false
        if (confirm.isBlank()) return false
        return true
    }
}
