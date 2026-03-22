package com.segnities007.note.presentation.state

import com.segnities007.note.domain.model.Note
import java.util.UUID

data class NotePageUiState(
    val pageId: String,
    val noteId: Long? = null,
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val title: String
        get() = extractNoteTitle(content)

    val isBlank: Boolean
        get() = content.isBlank()

    fun toDomain(): Note {
        return Note(
            id = noteId ?: 0L,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun blank(pageId: String = newDraftPageId()): NotePageUiState = NotePageUiState(pageId = pageId)
    }
}

fun Note.toUiPage(pageId: String = "note-$id"): NotePageUiState {
    return NotePageUiState(
        pageId = pageId,
        noteId = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun newDraftPageId(): String = "draft-${UUID.randomUUID()}"
