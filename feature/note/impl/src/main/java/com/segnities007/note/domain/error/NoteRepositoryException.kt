package com.segnities007.note.domain.error

/** ノート永続化・復号まわりの想定内失敗。 */
sealed class NoteRepositoryException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    /** SQLCipher またはセッション鍵がロックされている。 */
    data object DatabaseLocked : NoteRepositoryException("Database locked")

    /** 指定 ID の行が存在しない。 */
    data object NoteNotFound : NoteRepositoryException("Note not found")

    /** 危険環境のため読み書きを拒否された。 */
    data object UntrustedEnvironment : NoteRepositoryException("Untrusted environment")

    /** フィールド暗号の復号に失敗した。 */
    class DecryptionFailed(
        noteId: Long,
        cause: Throwable
    ) : NoteRepositoryException("Failed to decrypt note: $noteId", cause)
}
