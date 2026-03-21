package com.segnities007.note.data.local

import com.segnities007.note.domain.error.NoteRepositoryException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class NoteLocalDataSource(
    private val noteDatabaseProvider: NoteDatabaseProvider
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeRecords(): Flow<List<NoteLocalRecord>> {
        return noteDatabaseProvider.noteDaoFlow().flatMapLatest { noteDao ->
            noteDao?.getAllNotes()?.map { entities ->
                entities.map(NoteEntity::toLocalRecord)
            } ?: flowOf(emptyList())
        }
    }

    suspend fun getRecordById(id: Long): NoteLocalRecord? {
        return noteDatabaseProvider.noteDaoOrNull()?.getNoteById(id)?.toLocalRecord()
    }

    suspend fun saveRecord(record: NoteLocalRecord): Result<Long> {
        val noteDao = noteDatabaseProvider.noteDaoOrNull()
            ?: return Result.failure(NoteRepositoryException.DatabaseLocked)

        return runCatching {
            noteDao.insertNote(record.toEntity())
        }
    }

    suspend fun deleteRecord(id: Long): Result<Unit> {
        val noteDao = noteDatabaseProvider.noteDaoOrNull()
            ?: return Result.failure(NoteRepositoryException.DatabaseLocked)

        return runCatching {
            val entity = noteDao.getNoteById(id)
                ?: throw NoteRepositoryException.NoteNotFound
            noteDao.deleteNote(entity)
        }
    }
}

internal data class NoteLocalRecord(
    val id: Long,
    val encryptedContent: ByteArray,
    val iv: ByteArray,
    val createdAt: Long,
    val updatedAt: Long
)

private fun NoteEntity.toLocalRecord(): NoteLocalRecord {
    return NoteLocalRecord(
        id = id,
        encryptedContent = encryptedContent,
        iv = iv,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun NoteLocalRecord.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        encryptedContent = encryptedContent,
        iv = iv,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
