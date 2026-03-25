package com.segnities007.note.data.transfer

import android.content.Context
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.auth.domain.security.SensitiveOperationBlockedException
import com.segnities007.auth.domain.security.SensitiveOperationGuard
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.ExportedVaultCryptoState
import com.segnities007.note.api.NoteVaultTransferPort
import com.segnities007.note.data.local.NoteDatabase
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [NoteVaultTransferPort] の実装。パック済みボルトの書き出し／取り込みと
 * [DataCipher]・認証フローの連携を [Dispatchers.IO] で行う。
 */
internal class NoteVaultTransferPortImpl(
    private val context: Context,
    private val dataCipher: DataCipher,
    private val authRepository: AuthRepository,
    private val sensitiveOperationGuard: SensitiveOperationGuard
) : NoteVaultTransferPort {

    override suspend fun exportPackedVaultFile(password: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            ensureAllowed()
            authRepository.verifyPassword(password).getOrThrow()
            authRepository.lock().getOrThrow()
            try {
                val crypto = dataCipher.exportPersistedFieldCipherState()
                    ?: error("Unlock the app at least once before exporting.")
                val dbFile = context.getDatabasePath(NoteDatabase.DATABASE_NAME)
                check(dbFile.exists() && dbFile.length() > 0L) { "No database to export." }
                val outFile = File(context.cacheDir, "vault-send-${System.nanoTime()}.packed")
                FileOutputStream(outFile).use { fos ->
                    VaultTransferBundleCodec.packToStream(dbFile, crypto, fos)
                }
                outFile
            } finally {
                authRepository.unlock(password).getOrThrow()
            }
        }
    }

    override suspend fun importPackedVaultFile(packedFile: File, vaultPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureAllowed()
                authRepository.lock().getOrThrow()

                val dbFile = context.getDatabasePath(NoteDatabase.DATABASE_NAME)
                val backupDb = File(
                    context.cacheDir,
                    "vault-import-rollback-${System.nanoTime()}.db"
                )
                val hadExistingDb = dbFile.exists() && dbFile.length() > 0L
                if (hadExistingDb) {
                    dbFile.copyTo(backupDb, overwrite = true)
                }
                val previousCrypto = dataCipher.exportPersistedFieldCipherState()

                try {
                    NoteDatabase.delete(context)
                    val state = FileInputStream(packedFile).use { fis ->
                        DataInputStream(fis).use { dis ->
                            VaultTransferBundleCodec.unpackFromStream(dis, dbFile)
                        }
                    }
                    dataCipher.importPersistedFieldCipherState(state)
                    authRepository.adoptImportedVault(vaultPassword).getOrThrow()
                } catch (t: Throwable) {
                    rollbackVaultAfterFailedImport(
                        dbFile = dbFile,
                        backupDb = backupDb,
                        hadExistingDb = hadExistingDb,
                        previousCrypto = previousCrypto,
                    ).forEach { t.addSuppressed(it) }
                    runCatching {
                        if (hadExistingDb && backupDb.exists()) {
                            backupDb.delete()
                        }
                    }.onFailure { t.addSuppressed(it) }
                    throw t
                }

                if (hadExistingDb && backupDb.exists()) {
                    backupDb.delete()
                }
            }
        }

    private fun rollbackVaultAfterFailedImport(
        dbFile: File,
        backupDb: File,
        hadExistingDb: Boolean,
        previousCrypto: ExportedVaultCryptoState?,
    ): List<Throwable> {
        val failures = mutableListOf<Throwable>()
        runCatching { NoteDatabase.delete(context) }.onFailure { failures.add(it) }
        if (hadExistingDb && backupDb.exists()) {
            runCatching { backupDb.copyTo(dbFile, overwrite = true) }.onFailure { failures.add(it) }
        } else {
            runCatching { if (dbFile.exists()) check(dbFile.delete()) { "Failed to delete partial DB" } }
                .onFailure { failures.add(it) }
        }
        when {
            previousCrypto != null ->
                runCatching { dataCipher.importPersistedFieldCipherState(previousCrypto) }
                    .onFailure { failures.add(it) }
            else ->
                runCatching { dataCipher.clearState() }.onFailure { failures.add(it) }
        }
        return failures
    }

    private fun ensureAllowed() {
        try {
            sensitiveOperationGuard.ensureSensitiveOperationAllowed()
        } catch (_: SensitiveOperationBlockedException) {
            throw IllegalStateException("Untrusted environment")
        }
    }
}
