package com.segnities007.note.presentation.state

import com.segnities007.note.presentation.contract.NoteState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NotePageStateTransitionsTest {

    @Test
    fun ensureTrailingBlankPage_keepsNonTrailingBlankPages() {
        val preservedBlank = NotePageUiState.blank(pageId = "draft-middle")
        val trailingBlank = NotePageUiState.blank(pageId = "draft-tail")

        val pages = ensureTrailingBlankPage(
            listOf(
                NotePageUiState(
                    pageId = "note-1",
                    noteId = 1L,
                    content = "First"
                ),
                preservedBlank,
                trailingBlank
            )
        )

        assertEquals(listOf("note-1", "draft-middle", "draft-tail"), pages.map { it.pageId })
    }

    @Test
    fun resolvePageChange_removesBlankNonTrailingPageAndAdjustsTargetIndex() {
        val abandonedBlank = NotePageUiState.blank(pageId = "draft-middle")
        val trailingBlank = NotePageUiState.blank(pageId = "draft-tail")
        val state = NoteState(
            pages = listOf(
                NotePageUiState(
                    pageId = "note-1",
                    noteId = 1L,
                    content = "First"
                ),
                abandonedBlank,
                trailingBlank
            ),
            currentPageIndex = 1,
            isLoading = false
        )

        val resolution = state.resolvePageChange(targetIndex = 2)

        assertSame(abandonedBlank, resolution.removedPage)
        assertEquals(listOf("note-1", "draft-tail"), resolution.state.pages.map { it.pageId })
        assertEquals(1, resolution.state.currentPageIndex)
    }

    @Test
    fun resolvePageChange_keepsTrailingBlankPageWhenLeavingIt() {
        val trailingBlank = NotePageUiState.blank(pageId = "draft-tail")
        val state = NoteState(
            pages = listOf(
                NotePageUiState(
                    pageId = "note-1",
                    noteId = 1L,
                    content = "First"
                ),
                trailingBlank
            ),
            currentPageIndex = 1,
            isLoading = false
        )

        val resolution = state.resolvePageChange(targetIndex = 0)

        assertNull(resolution.removedPage)
        assertEquals(0, resolution.state.currentPageIndex)
        assertTrue(resolution.state.pages.last().isBlank)
    }
}
