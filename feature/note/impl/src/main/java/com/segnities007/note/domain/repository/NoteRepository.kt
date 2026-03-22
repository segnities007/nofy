package com.segnities007.note.domain.repository

import com.segnities007.note.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Result<Note>
    suspend fun deleteNote(id: Long): Result<Unit>
}
