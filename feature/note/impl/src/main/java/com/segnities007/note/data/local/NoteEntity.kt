package com.segnities007.note.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room に保存するノート 1 行（本文は AES-GCM 暗号文 + IV）。 */
@Entity(tableName = "notes")
internal data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encryptedContent: ByteArray,
    val iv: ByteArray,
    val createdAt: Long,
    val updatedAt: Long
)
