package com.segnities007.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encryptedContent: ByteArray,
    val iv: ByteArray,
    val createdAt: Long,
    val updatedAt: Long
)
