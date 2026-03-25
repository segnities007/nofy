package com.segnities007.note.presentation.state

import com.segnities007.note.domain.model.Note
import com.segnities007.note.presentation.contract.NoteState

/** ページ切替後の [NoteState] と、空ページ削除で消えたページ（あれば）。 */
internal data class PageChangeResolution(
    val state: NoteState,
    val removedPage: NotePageUiState? = null
)

internal fun List<Note>.toNotePages(): List<NotePageUiState> {
    return ensureTrailingBlankPage(map { it.toUiPage() })
}

internal fun List<NotePageUiState>.findPage(pageId: String?): NotePageUiState? {
    return pageId?.let { targetPageId ->
        find { it.pageId == targetPageId }
    }
}

internal fun ensureTrailingBlankPage(
    pages: List<NotePageUiState>
): List<NotePageUiState> {
    val normalized = pages.toMutableList()

    if (normalized.isEmpty() || normalized.last().noteId != null || normalized.last().content.isNotBlank()) {
        normalized += NotePageUiState.blank()
    }

    return normalized
}

internal fun replacePage(
    pages: List<NotePageUiState>,
    pageId: String,
    replacement: NotePageUiState
): List<NotePageUiState> {
    return ensureTrailingBlankPage(
        pages.map { current ->
            if (current.pageId == pageId) replacement else current
        }
    )
}

internal fun buildPagesAfterRemoval(
    pages: List<NotePageUiState>,
    pageId: String
): List<NotePageUiState> {
    return ensureTrailingBlankPage(
        pages.filterNot { it.pageId == pageId }
    )
}

internal fun resolveCurrentPageIndexAfterRemoval(
    state: NoteState,
    removedIndex: Int,
    pages: List<NotePageUiState>
): Int {
    return when {
        pages.isEmpty() -> 0
        state.currentPageIndex > removedIndex -> state.currentPageIndex - 1
        state.currentPageIndex == removedIndex -> removedIndex.coerceAtMost(pages.lastIndex)
        else -> state.currentPageIndex
    }
}

internal fun NoteState.resolvePageChange(
    targetIndex: Int
): PageChangeResolution {
    if (targetIndex !in pages.indices || targetIndex == currentPageIndex) {
        return PageChangeResolution(state = this)
    }

    val currentPage = currentPage ?: return PageChangeResolution(
        state = copy(currentPageIndex = targetIndex)
    )
    val shouldRemoveCurrentPage = currentPage.isBlank && currentPageIndex != pages.lastIndex

    if (!shouldRemoveCurrentPage) {
        return PageChangeResolution(
            state = copy(currentPageIndex = targetIndex)
        )
    }

    val pagesAfterRemoval = buildPagesAfterRemoval(pages, currentPage.pageId)
    val adjustedTargetIndex = if (targetIndex > currentPageIndex) {
        targetIndex - 1
    } else {
        targetIndex
    }.coerceIn(0, pagesAfterRemoval.lastIndex)

    return PageChangeResolution(
        state = copy(
            pages = pagesAfterRemoval,
            currentPageIndex = adjustedTargetIndex
        ),
        removedPage = currentPage
    )
}
