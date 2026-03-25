package com.segnities007.note.presentation.contract

/**
 * ノート画面から [com.segnities007.note.presentation.viewmodel.NoteViewModel] へ渡すユーザー操作。
 */
sealed interface NoteIntent {
    /** 指定ページの本文を更新する（デバウンス保存へつながる）。 */
    data class EditContent(val pageId: String, val content: String) : NoteIntent

    /** ページャー等で現在ページインデックスが変わった。 */
    data class PageChanged(val index: Int) : NoteIntent

    /** ページを削除し、残りを再構成する。 */
    data class DeletePage(val pageId: String) : NoteIntent

    /** リポジトリからノート一覧を再読込する。 */
    data object Reload : NoteIntent

    /** Markdown プレビュー表示のオンオフを切り替える。 */
    data object TogglePreview : NoteIntent

    /** アプリをロックする。 */
    data object Lock : NoteIntent

    /** 設定画面へのナビを要求する。 */
    data object NavigateToSettings : NoteIntent

    /** 次のページへ進む（範囲内なら）。 */
    data object NavigateToNextPage : NoteIntent

    /** 前のページへ戻る（範囲内なら）。 */
    data object NavigateToPreviousPage : NoteIntent

    /** 表示済みの [NoteUserMessage] をクリアする。 */
    data object ConsumeUserMessage : NoteIntent

    /** 処理済みの [NoteNavigationRequest] をクリアする。 */
    data object ConsumePendingNavigation : NoteIntent
}
