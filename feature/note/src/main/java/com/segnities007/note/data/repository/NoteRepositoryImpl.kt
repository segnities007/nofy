package com.segnities007.note.data.repository

import com.segnities007.crypto.DataCipher
import com.segnities007.database.DatabaseProvider
import com.segnities007.database.entity.NoteEntity
import com.segnities007.note.domain.error.NoteRepositoryException
import com.segnities007.note.domain.model.Note
import com.segnities007.note.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * 物理的暗号化(SQLCipher)とフィールド暗号化(AES-GCM)を統合したリポジトリ。
 * AGENTS.mdのSecurity by Designに基づき、多層防御を実装。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepositoryImpl(
    private val databaseProvider: DatabaseProvider,
    private val dataCipher: DataCipher
) : NoteRepository {

    private fun getNoteDao() = databaseProvider.noteDaoOrNull()

    override fun getNotes(): Flow<List<Note>> {
        return databaseProvider.noteDaoFlow().flatMapLatest { noteDao ->
            noteDao?.getAllNotes()?.map { entities ->
                entities.map { decryptEntity(it) }
            } ?: flowOf(emptyList())
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return getNoteDao()?.getNoteById(id)?.let { decryptEntity(it) }
    }

    override suspend fun saveNote(note: Note): Result<Note> {
        val dao = getNoteDao() ?: return Result.failure(NoteRepositoryException.DatabaseLocked)
        return try {
            val (encrypted, iv) = dataCipher.encrypt(note.content)
            val updatedAt = System.currentTimeMillis()
            val entity = NoteEntity(
                id = note.id,
                encryptedContent = encrypted,
                iv = iv,
                createdAt = note.createdAt,
                updatedAt = updatedAt
            )
            val savedId = dao.insertNote(entity)
            Result.success(
                note.copy(
                    id = if (note.id == 0L) savedId else note.id,
                    updatedAt = updatedAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: Long): Result<Unit> {
        val dao = getNoteDao() ?: return Result.failure(NoteRepositoryException.DatabaseLocked)
        return try {
            val entity = dao.getNoteById(id)
            if (entity != null) {
                dao.deleteNote(entity)
                Result.success(Unit)
            } else {
                Result.failure(NoteRepositoryException.NoteNotFound)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decryptEntity(entity: NoteEntity): Note {
        val content = try {
            dataCipher.decrypt(entity.encryptedContent, entity.iv)
        } catch (e: Exception) {
            throw NoteRepositoryException.DecryptionFailed(entity.id, e)
        }
        return Note(
            id = entity.id,
            content = content,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
