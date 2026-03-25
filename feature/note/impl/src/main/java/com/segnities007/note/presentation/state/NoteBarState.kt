package com.segnities007.note.presentation.state

import com.segnities007.note.presentation.contract.NoteState

/** フローティングトップ／ボトムバーに渡す集約表示用状態。 */
internal data class NoteBarState(
    val title: String,
    val canDelete: Boolean,
    val currentPage: Int,
    val totalPages: Int,
    val canNavigatePrevious: Boolean,
    val canNavigateNext: Boolean,
    val canShowBars: Boolean
)

/** [NoteState] からバー用のタイトル・ページャ・削除可否を導出する。 */
internal fun NoteState.toBarState(untitledTitle: String): NoteBarState {
    val currentPage = currentPage
    return NoteBarState(
        title = currentPage?.title?.ifBlank { untitledTitle } ?: untitledTitle,
        canDelete = currentPage?.let { it.noteId != null || it.content.isNotBlank() } == true,
        currentPage = currentPageIndex,
        totalPages = pages.size,
        canNavigatePrevious = currentPageIndex > 0,
        canNavigateNext = currentPageIndex < pages.lastIndex,
        canShowBars = !isLoading && (error == null || pages.any { !it.isBlank })
    )
}
