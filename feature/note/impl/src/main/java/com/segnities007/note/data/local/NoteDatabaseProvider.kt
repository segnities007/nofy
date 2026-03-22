package com.segnities007.note.data.local

import android.content.Context
import com.segnities007.database.SecureDatabaseController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.nio.charset.StandardCharsets
import net.zetetic.database.sqlcipher.SQLiteDatabase

internal class NoteDatabaseProvider(
    private val context: Context
) : SecureDatabaseController {
    private val database = MutableStateFlow<NoteDatabase?>(null)

    init {
        loadSqlCipherLibrary()
    }

    fun noteDaoFlow(): Flow<NoteDao?> = database.asStateFlow().map { it?.noteDao() }

    fun noteDaoOrNull(): NoteDao? = database.value?.noteDao()

    override fun unlock(passphrase: ByteArray) {
        if (database.value == null) {
            database.value = try {
                NoteDatabase.build(context, passphrase)
            } finally {
                passphrase.fill(0)
            }
        }
    }

    override fun changePassphrase(currentPassphrase: String, newPassphrase: String) {
        lock()

        if (!databaseFile.exists()) {
            unlock(newPassphrase.toByteArray(StandardCharsets.UTF_8))
            return
        }

        val currentPassphraseBytes = currentPassphrase.toByteArray(StandardCharsets.UTF_8)
        val newPassphraseBytes = newPassphrase.toByteArray(StandardCharsets.UTF_8)
        val sqlCipherDatabase = SQLiteDatabase.openDatabase(
            databaseFile.path,
            currentPassphraseBytes,
            null,
            SQLiteDatabase.OPEN_READWRITE,
            null
        )

        try {
            sqlCipherDatabase.changePassword(newPassphraseBytes)
        } finally {
            currentPassphraseBytes.fill(0)
            newPassphraseBytes.fill(0)
            sqlCipherDatabase.close()
        }

        unlock(newPassphrase.toByteArray(StandardCharsets.UTF_8))
    }

    override fun lock() {
        database.value?.close()
        database.value = null
    }

    override fun deleteDatabaseFiles() {
        lock()
        NoteDatabase.delete(context)
    }

    private val databaseFile
        get() = context.getDatabasePath(NoteDatabase.DATABASE_NAME)

    private fun loadSqlCipherLibrary() {
        System.loadLibrary(SQL_CIPHER_LIBRARY_NAME)
    }

    private companion object {
        const val SQL_CIPHER_LIBRARY_NAME = "sqlcipher"
    }
}
