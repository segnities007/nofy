package com.segnities007.database

import android.content.Context
import com.segnities007.database.dao.NoteDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * データベースのインスタンスを保持し、アンロックされるまで提供を制限するクラス。
 */
class DatabaseProvider(private val context: Context) {
    private val _database = MutableStateFlow<NofyDatabase?>(null)
    val database: StateFlow<NofyDatabase?> = _database.asStateFlow()

    fun noteDaoFlow(): Flow<NoteDao?> = database.map { it?.noteDao() }

    fun noteDaoOrNull(): NoteDao? = _database.value?.noteDao()

    /**
     * パスフレーズを使用してデータベースを初期化（アンロック）する。
     */
    fun unlock(passphrase: ByteArray) {
        if (_database.value == null) {
            _database.value = NofyDatabase.build(context, passphrase)
        }
    }

    /**
     * データベースを閉じ、メモリからインスタンスを消去する。
     */
    fun lock() {
        _database.value?.close()
        _database.value = null
    }

    fun deleteDatabaseFiles() {
        lock()
        NofyDatabase.delete(context)
    }
}
