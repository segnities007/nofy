package com.segnities007.note.presentation

sealed interface NoteIntent {
    data class EditContent(val pageId: String, val content: String) : NoteIntent
    data class PageChanged(val index: Int) : NoteIntent
    data class DeletePage(val pageId: String) : NoteIntent
    data object Reload : NoteIntent
    data object TogglePreview : NoteIntent
    data object Lock : NoteIntent
    data object NavigateToSettings : NoteIntent
    data object NavigateToNextPage : NoteIntent
    data object NavigateToPreviousPage : NoteIntent
}
