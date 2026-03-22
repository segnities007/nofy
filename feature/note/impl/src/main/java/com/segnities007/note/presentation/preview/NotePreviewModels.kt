package com.segnities007.note.presentation.preview

import com.segnities007.note.presentation.contract.NoteState
import com.segnities007.note.presentation.state.NotePageUiState

internal fun previewNotePages(): List<NotePageUiState> {
    return listOf(
        NotePageUiState(
            pageId = "note-1",
            noteId = 1L,
            content = "# Secure note\n- First item\n- Second item"
        ),
        NotePageUiState.blank(pageId = "draft")
    )
}

internal fun previewNoteState(
    isPreviewEnabled: Boolean = false,
    isLoading: Boolean = false,
    error: String? = null
): NoteState {
    return NoteState(
        pages = previewNotePages(),
        currentPageIndex = 0,
        isPreviewEnabled = isPreviewEnabled,
        isLoading = isLoading,
        error = error
    )
}
