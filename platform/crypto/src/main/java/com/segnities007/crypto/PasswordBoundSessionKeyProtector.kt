package com.segnities007.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val AesAlgorithm = "AES"
private const val AesTransformation = "AES/GCM/NoPadding"
private const val GcmTagLengthBits = 128
private const val SessionKeySizeBytes = 32
private const val SaltSizeBytes = 16
private const val ArgonIterations = 3
private const val ArgonMemoryKib = 65_536
private const val ArgonParallelism = 4

private fun ByteBuffer.toByteArray(): ByteArray {
    val bytes = ByteArray(remaining())
    get(bytes)
    return bytes
}

/** 永続化するセッション鍵のラップ結果（ソルト・IV・密文）。 */
internal data class WrappedSessionKeyState(
    val salt: ByteArray,
    val iv: ByteArray,
    val wrappedKey: ByteArray
)

/** メモリ上の平文セッション鍵と、保存用ラップ状態のペア。 */
internal data class SessionKeyStateSnapshot(
    val sessionKey: ByteArray,
    val wrappedState: WrappedSessionKeyState
)

/**
 * パスワードとソルトからセッション用バイト列を導出する（Argon2 実装の差し替え点）。
 */
internal fun interface SessionKeyDeriver {
    /**
     * [password] と [salt] からセッション鍵導出用の固定長バイト列を返す（通常 Argon2）。
     */
    fun derive(
        password: ByteArray,
        salt: ByteArray
    ): ByteArray
}

/** パスワード由来の KEK でランダムなセッション鍵をラップ／アンラップする。 */
internal class PasswordBoundSessionKeyProtector(
    private val sessionKeyDeriver: SessionKeyDeriver = ArgonSessionKeyDeriver,
    private val secureRandom: SecureRandom = SecureRandom()
) {
    /** 新規セッション鍵を生成し、[password] でラップした状態を返す。 */
    fun create(password: ByteArray): SessionKeyStateSnapshot {
        val sessionKey = ByteArray(SessionKeySizeBytes).also(secureRandom::nextBytes)
        val salt = ByteArray(SaltSizeBytes).also(secureRandom::nextBytes)
        return SessionKeyStateSnapshot(
            sessionKey = sessionKey,
            wrappedState = wrapSessionKey(
                password = password,
                sessionKey = sessionKey,
                salt = salt
            )
        )
    }

    /** [wrappedState] からセッション鍵の平文バイト列を復号する。 */
    fun unwrap(
        password: ByteArray,
        wrappedState: WrappedSessionKeyState
    ): ByteArray {
        val keyEncryptionKey = deriveKeyEncryptionKey(
            password = password,
            salt = wrappedState.salt
        )
        return try {
            val cipher = Cipher.getInstance(AesTransformation)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(keyEncryptionKey, AesAlgorithm),
                GCMParameterSpec(GcmTagLengthBits, wrappedState.iv)
            )
            cipher.doFinal(wrappedState.wrappedKey)
        } finally {
            keyEncryptionKey.fill(0)
        }
    }

    /** 現在のパスワードでアンラップしたセッション鍵を、新パスワードで再ラップする。 */
    fun rewrap(
        currentPassword: ByteArray,
        newPassword: ByteArray,
        wrappedState: WrappedSessionKeyState
    ): SessionKeyStateSnapshot {
        val sessionKey = unwrap(
            password = currentPassword,
            wrappedState = wrappedState
        )
        val newSalt = ByteArray(SaltSizeBytes).also(secureRandom::nextBytes)
        return SessionKeyStateSnapshot(
            sessionKey = sessionKey,
            wrappedState = wrapSessionKey(
                password = newPassword,
                sessionKey = sessionKey,
                salt = newSalt
            )
        )
    }

    private fun wrapSessionKey(
        password: ByteArray,
        sessionKey: ByteArray,
        salt: ByteArray
    ): WrappedSessionKeyState {
        val keyEncryptionKey = deriveKeyEncryptionKey(
            password = password,
            salt = salt
        )
        return try {
            val cipher = Cipher.getInstance(AesTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyEncryptionKey, AesAlgorithm))
            WrappedSessionKeyState(
                salt = salt,
                iv = cipher.iv,
                wrappedKey = cipher.doFinal(sessionKey)
            )
        } finally {
            keyEncryptionKey.fill(0)
        }
    }

    private fun deriveKeyEncryptionKey(
        password: ByteArray,
        salt: ByteArray
    ): ByteArray {
        return sessionKeyDeriver.derive(password, salt)
    }

}

private object ArgonSessionKeyDeriver : SessionKeyDeriver {
    private val argon2 = Argon2Kt()

    override fun derive(
        password: ByteArray,
        salt: ByteArray
    ): ByteArray {
        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password,
            salt = salt,
            tCostInIterations = ArgonIterations,
            mCostInKibibyte = ArgonMemoryKib,
            parallelism = ArgonParallelism,
            hashLengthInBytes = SessionKeySizeBytes
        )
        return result.rawHash.toByteArray()
    }
}
