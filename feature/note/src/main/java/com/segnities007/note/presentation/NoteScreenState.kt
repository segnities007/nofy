package com.segnities007.note.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun rememberNoteScreenState(): NoteScreenState {
    return remember { NoteScreenState() }
}

@Stable
internal class NoteScreenState {
    var areBarsVisible by mutableStateOf(true)
        private set

    var pendingDeletePageId: String? by mutableStateOf(null)
        private set

    fun showBars() {
        areBarsVisible = true
    }

    fun updateBarsVisibility(visible: Boolean) {
        areBarsVisible = visible
    }

    fun requestDelete(pageId: String) {
        pendingDeletePageId = pageId
        showBars()
    }

    fun dismissDeleteDialog() {
        pendingDeletePageId = null
    }
}
