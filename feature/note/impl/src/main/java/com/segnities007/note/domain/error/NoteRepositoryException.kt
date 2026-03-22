package com.segnities007.note.domain.error

sealed class NoteRepositoryException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    data object DatabaseLocked : NoteRepositoryException("Database locked")
    data object NoteNotFound : NoteRepositoryException("Note not found")
    data object UntrustedEnvironment : NoteRepositoryException("Untrusted environment")

    class DecryptionFailed(
        noteId: Long,
        cause: Throwable
    ) : NoteRepositoryException("Failed to decrypt note: $noteId", cause)
}
