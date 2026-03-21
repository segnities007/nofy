package com.segnities007.note.domain.model

data class Note(
    val id: Long = 0,
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
