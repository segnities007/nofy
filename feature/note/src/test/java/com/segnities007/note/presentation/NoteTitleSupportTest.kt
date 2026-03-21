package com.segnities007.note.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTitleSupportTest {

    @Test
    fun extractNoteTitle_usesFirstNonBlankLine() {
        val title = extractNoteTitle(
            """

            # Secure notebook title
            body
            """.trimIndent()
        )

        assertEquals("Secure notebook title", title)
    }

    @Test
    fun extractNoteTitle_returnsEmptyForBlankContent() {
        assertEquals("", extractNoteTitle(""))
    }
}
