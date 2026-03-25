package com.segnities007.note.api

import java.io.File

/**
 * 同一 LAN 上の QR 確立転送用に、SQLCipher DB とフィールド暗号状態を束ねたパックの入出力。
 */
interface NoteVaultTransferPort {
    /**
     * 現在のボルトを [password] で開き、転送用にパックした一時ファイルを返す。
     */
    suspend fun exportPackedVaultFile(password: String): Result<File>

    /**
     * [packedFile] を [vaultPassword] で復元し、この端末のボルトへ取り込む。
     */
    suspend fun importPackedVaultFile(packedFile: File, vaultPassword: String): Result<Unit>
}
