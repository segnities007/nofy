package com.segnities007.note.data.repository

import com.segnities007.auth.domain.security.SensitiveOperationBlockedException
import com.segnities007.auth.domain.security.SensitiveOperationGuard
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.DataCipherException
import com.segnities007.note.data.local.NoteLocalDataSource
import com.segnities007.note.data.local.NoteLocalRecord
import com.segnities007.note.domain.error.NoteRepositoryException
import com.segnities007.note.domain.model.Note
import com.segnities007.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 物理的暗号化(SQLCipher)とフィールド暗号化(AES-GCM)を統合したリポジトリ。
 * AGENTS.mdのSecurity by Designに基づき、多層防御を実装。
 */
internal class NoteRepositoryImpl(
    private val noteLocalDataSource: NoteLocalDataSource,
    private val dataCipher: DataCipher,
    private val sensitiveOperationGuard: SensitiveOperationGuard
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return noteLocalDataSource.observeRecords().map { records ->
            ensureSensitiveOperationAllowed()
            records.map(::decryptRecord)
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        ensureSensitiveOperationAllowed()
        return noteLocalDataSource.getRecordById(id)?.let(::decryptRecord)
    }

    override suspend fun saveNote(note: Note): Result<Note> {
        return try {
            ensureSensitiveOperationAllowed()
            val (encrypted, iv) = dataCipher.encrypt(note.content)
            val updatedAt = System.currentTimeMillis()
            val record = NoteLocalRecord(
                id = note.id,
                encryptedContent = encrypted,
                iv = iv,
                createdAt = note.createdAt,
                updatedAt = updatedAt
            )
            val savedId = noteLocalDataSource.saveRecord(record).getOrThrow()
            Result.success(
                note.copy(
                    id = if (note.id == 0L) savedId else note.id,
                    updatedAt = updatedAt
                )
            )
        } catch (e: DataCipherException.SessionLocked) {
            Result.failure(NoteRepositoryException.DatabaseLocked)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: Long): Result<Unit> {
        return try {
            ensureSensitiveOperationAllowed()
            noteLocalDataSource.deleteRecord(id)
        } catch (error: NoteRepositoryException.UntrustedEnvironment) {
            Result.failure(error)
        }
    }

    private fun decryptRecord(record: NoteLocalRecord): Note {
        val content = try {
            dataCipher.decrypt(record.encryptedContent, record.iv)
        } catch (_: DataCipherException.SessionLocked) {
            throw NoteRepositoryException.DatabaseLocked
        } catch (e: Exception) {
            throw NoteRepositoryException.DecryptionFailed(record.id, e)
        }
        return Note(
            id = record.id,
            content = content,
            createdAt = record.createdAt,
            updatedAt = record.updatedAt
        )
    }

    private fun ensureSensitiveOperationAllowed() {
        try {
            sensitiveOperationGuard.ensureSensitiveOperationAllowed()
        } catch (_: SensitiveOperationBlockedException) {
            throw NoteRepositoryException.UntrustedEnvironment
        }
    }
}
