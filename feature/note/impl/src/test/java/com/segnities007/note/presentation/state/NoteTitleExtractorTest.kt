package com.segnities007.note.presentation.state

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTitleExtractorTest {

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
