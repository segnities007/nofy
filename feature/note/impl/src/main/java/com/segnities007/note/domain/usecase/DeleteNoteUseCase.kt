package com.segnities007.note.domain.usecase

import com.segnities007.note.domain.repository.NoteRepository

/** 主キーでノートを削除する。 */
internal class DeleteNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long): Result<Unit> = noteRepository.deleteNote(noteId)
}
