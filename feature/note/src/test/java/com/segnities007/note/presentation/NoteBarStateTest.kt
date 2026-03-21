package com.segnities007.note.presentation.state

import com.segnities007.note.presentation.contract.NoteState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteBarStateTest {

    @Test
    fun toBarState_usesUntitledFallbackAndHidesBarsWhileLoading() {
        val state = NoteState(
            pages = listOf(NotePageUiState.blank(pageId = "draft")),
            isLoading = true
        )

        val barState = state.toBarState(untitledTitle = "Untitled")

        assertEquals("Untitled", barState.title)
        assertFalse(barState.canDelete)
        assertFalse(barState.canShowBars)
    }

    @Test
    fun toBarState_enablesDeleteForSavedPage() {
        val state = NoteState(
            pages = listOf(
                NotePageUiState(
                    pageId = "note-1",
                    noteId = 1L,
                    content = "# Title"
                ),
                NotePageUiState.blank(pageId = "draft")
            ),
            isLoading = false
        )

        val barState = state.toBarState(untitledTitle = "Untitled")

        assertEquals("Title", barState.title)
        assertTrue(barState.canDelete)
        assertTrue(barState.canNavigateNext)
    }
}
