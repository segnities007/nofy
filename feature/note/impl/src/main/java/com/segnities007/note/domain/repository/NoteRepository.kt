package com.segnities007.note.domain.repository

import com.segnities007.note.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * ノート一覧の購読と CRUD をドメイン層へ公開するリポジトリ契約。
 */
interface NoteRepository {
    /** 全ノートを作成日昇順で購読する。 */
    fun getNotes(): Flow<List<Note>>

    /** ID で 1 件取得する。存在しなければ `null`。 */
    suspend fun getNoteById(id: Long): Note?

    /** 挿入または更新し、永続化後のエンティティ（採番 ID 含む）を返す。 */
    suspend fun saveNote(note: Note): Result<Note>

    /** ID を指定して削除する。 */
    suspend fun deleteNote(id: Long): Result<Unit>
}
