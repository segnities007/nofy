package com.segnities007.note.presentation.state

internal fun extractNoteTitle(content: String): String {
    val candidate = content.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() }
        ?.removePrefix("#")
        ?.trim()
        ?: return ""

    return if (candidate.length <= 32) candidate else candidate.take(32).trimEnd() + "..."
}
