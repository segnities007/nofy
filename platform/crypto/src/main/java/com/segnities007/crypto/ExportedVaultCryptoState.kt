package com.segnities007.crypto

/**
 * [DataCipher] が SharedPreferences に保持するラップ済みセッション鍵。
 * ボルト移行時に SQLCipher DB とセットで転送する。
 */
data class ExportedVaultCryptoState(
    val salt: ByteArray,
    val iv: ByteArray,
    val wrappedSessionKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ExportedVaultCryptoState
        return salt.contentEquals(other.salt) &&
            iv.contentEquals(other.iv) &&
            wrappedSessionKey.contentEquals(other.wrappedSessionKey)
    }

    override fun hashCode(): Int {
        var result = salt.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + wrappedSessionKey.contentHashCode()
        return result
    }
}
