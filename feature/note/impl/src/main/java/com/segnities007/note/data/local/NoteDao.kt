package com.segnities007.note.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room 永続化用のノートテーブルへのアクセス。
 */
@Dao
internal interface NoteDao {
    /** 全行を作成日昇順で購読する。 */
    @Query("SELECT * FROM notes ORDER BY createdAt ASC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /** 主キーで 1 行取得する。 */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    /** UPSERT し、行 ID を返す。 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    /** エンティティと一致する行を削除する。 */
    @Delete
    suspend fun deleteNote(note: NoteEntity)
}
