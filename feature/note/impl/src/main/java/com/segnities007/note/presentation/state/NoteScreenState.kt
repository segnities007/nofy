package com.segnities007.note.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface

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

@NofyPreview
@Composable
private fun NoteScreenStatePreview() {
    val screenState = rememberNoteScreenState()

    NofyPreviewSurface {
        NofyText(
            text = "bars=${screenState.areBarsVisible}, delete=${screenState.pendingDeletePageId ?: "none"}"
        )
    }
}
