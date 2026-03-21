package com.segnities007.note.data.local

import android.content.Context
import com.segnities007.database.SecureDatabaseController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

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

    override fun lock() {
        database.value?.close()
        database.value = null
    }

    override fun deleteDatabaseFiles() {
        lock()
        NoteDatabase.delete(context)
    }
}
