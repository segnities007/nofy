package com.segnities007.note.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
internal data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encryptedContent: ByteArray,
    val iv: ByteArray,
    val createdAt: Long,
    val updatedAt: Long
)
