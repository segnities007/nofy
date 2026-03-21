package com.segnities007.note.data.local

import android.content.Context
import com.segnities007.database.SecureDatabaseController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import net.sqlcipher.database.SQLiteDatabase

internal class NoteDatabaseProvider(
    private val context: Context
) : SecureDatabaseController {
    private val database = MutableStateFlow<NoteDatabase?>(null)

    fun noteDaoFlow(): Flow<NoteDao?> = database.asStateFlow().map { it?.noteDao() }

    fun noteDaoOrNull(): NoteDao? = database.value?.noteDao()

    override fun unlock(passphrase: ByteArray) {
        if (database.value == null) {
            database.value = NoteDatabase.build(context, passphrase)
        }
    }

    override fun changePassphrase(currentPassphrase: String, newPassphrase: String) {
        lock()

        if (!databaseFile.exists()) {
            unlock(newPassphrase.toByteArray())
            return
        }

        SQLiteDatabase.loadLibs(context)
        val sqlCipherDatabase = SQLiteDatabase.openDatabase(
            databaseFile.path,
            currentPassphrase.toCharArray(),
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        try {
            sqlCipherDatabase.changePassword(newPassphrase.toCharArray())
        } finally {
            sqlCipherDatabase.close()
        }

        unlock(newPassphrase.toByteArray())
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
}
