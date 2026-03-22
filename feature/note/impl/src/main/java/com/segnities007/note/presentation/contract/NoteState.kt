package com.segnities007.note.presentation.contract

import androidx.annotation.StringRes
import com.segnities007.note.presentation.state.NotePageUiState

sealed interface NoteUserMessage {
    data class ToastRes(@param:StringRes val messageRes: Int) : NoteUserMessage
}

sealed interface NoteNavigationRequest {
    data object ToSettings : NoteNavigationRequest
    data object ToLogin : NoteNavigationRequest
}

data class NoteState(
    val pages: List<NotePageUiState> = listOf(NotePageUiState.blank()),
    val currentPageIndex: Int = 0,
    val isPreviewEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val pendingUserMessage: NoteUserMessage? = null,
    val pendingNavigation: NoteNavigationRequest? = null
) {
    val currentPage: NotePageUiState?
        get() = pages.getOrNull(currentPageIndex)
}
