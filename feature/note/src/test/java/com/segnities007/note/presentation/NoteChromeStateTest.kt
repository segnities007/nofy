package com.segnities007.note.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteChromeStateTest {

    @Test
    fun toChromeState_usesUntitledFallbackAndHidesBarsWhileLoading() {
        val state = NoteState(
            pages = listOf(NotePageUiState.blank(pageId = "draft")),
            isLoading = true
        )

        val chromeState = state.toChromeState(untitledTitle = "Untitled")

        assertEquals("Untitled", chromeState.title)
        assertFalse(chromeState.canDelete)
        assertFalse(chromeState.canShowBars)
    }

    @Test
    fun toChromeState_enablesDeleteForSavedPage() {
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

        val chromeState = state.toChromeState(untitledTitle = "Untitled")

        assertEquals("Title", chromeState.title)
        assertTrue(chromeState.canDelete)
        assertTrue(chromeState.canNavigateNext)
    }
}
