package com.segnities007.note.presentation

import com.segnities007.note.domain.model.Note

internal fun List<Note>.toNotePages(): List<NotePageUiState> {
    return ensureTrailingBlankPage(map { it.toUiPage() })
}

internal fun List<NotePageUiState>.findPage(pageId: String?): NotePageUiState? {
    return pageId?.let { targetPageId ->
        find { it.pageId == targetPageId }
    }
}

internal fun NotePageUiState.shouldDeleteForBlankContent(content: String): Boolean {
    return noteId != null && content.isBlank()
}

internal fun ensureTrailingBlankPage(
    pages: List<NotePageUiState>
): List<NotePageUiState> {
    val normalized = pages.filterIndexed { index, page ->
        !(page.noteId == null && page.content.isBlank() && index != pages.lastIndex)
    }.toMutableList()

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
