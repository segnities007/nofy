package com.segnities007.note.presentation.contract

import com.segnities007.note.presentation.state.NotePageUiState

data class NoteState(
    val pages: List<NotePageUiState> = listOf(NotePageUiState.blank()),
    val currentPageIndex: Int = 0,
    val isPreviewEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentPage: NotePageUiState?
        get() = pages.getOrNull(currentPageIndex)
}
