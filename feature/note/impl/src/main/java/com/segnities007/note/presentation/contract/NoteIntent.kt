package com.segnities007.note.presentation.contract

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
    data object ConsumeUserMessage : NoteIntent
    data object ConsumePendingNavigation : NoteIntent
}
