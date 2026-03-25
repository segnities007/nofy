package com.segnities007.note.domain.usecase

import com.segnities007.note.domain.model.Note
import com.segnities007.note.domain.repository.NoteRepository

/** 1 件のノートを永続化する。 */
internal class SaveNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Note> = noteRepository.saveNote(note)
}
