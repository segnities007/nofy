package com.segnities007.note.domain.usecase

import com.segnities007.note.domain.model.Note
import com.segnities007.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first

/** ノート一覧の現在スナップショットを 1 回取得する（エディタ初期化用）。 */
internal class LoadNotesSnapshotUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(): Result<List<Note>> {
        return runCatching { noteRepository.getNotes().first() }
    }
}
