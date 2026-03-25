package com.segnities007.crypto

import android.content.Context
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * メモ本文のフィールド暗号化をアプリ用パスワードに束縛する。
 *
 * - 新規データは password-derived KEK で unwrap した session key で暗号化
 * - 旧 data_encryption_key で暗号化された既存データは復号時のみ fallback
 * - fallback で読めたデータは repository 側で新方式へ再暗号化する
 */
class DataCipher internal constructor(
    context: Context,
    private val sessionKeyProtector: PasswordBoundSessionKeyProtector = PasswordBoundSessionKeyProtector()
) {
    private val stateStore = DataCipherStateStore(context.applicationContext)
    private val keyStore = KeyStore.getInstance(AndroidKeyStore).apply { load(null) }
    private val sessionKeyLock = Any()
    private var sessionKey: ByteArray? = null

    /**
     * [password] でラップ済みセッション鍵を復号しメモリに保持する。初回は状態を生成して保存する。
     */
    fun unlockSession(password: String) {
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
        val nextSessionKey = try {
            val persistedState = stateStore.load()
            if (persistedState != null) {
                sessionKeyProtector.unwrap(
                    password = passwordBytes,
                    wrappedState = persistedState
                )
            } else {
                val createdState = sessionKeyProtector.create(passwordBytes)
                stateStore.save(createdState.wrappedState)
                createdState.sessionKey
            }
        } catch (error: Exception) {
            throw DataCipherException.KeyStateCorrupted(error)
        } finally {
            passwordBytes.fill(0)
        }

        replaceSessionKey(nextSessionKey)
    }

    /** メモリ上のセッション鍵をゼロクリアして破棄する。 */
    fun lockSession() {
        synchronized(sessionKeyLock) {
            sessionKey?.fill(0)
            sessionKey = null
        }
    }

    /** 永続状態とセッション鍵の両方を消去する（リセット時）。 */
    fun clearState() {
        lockSession()
        try {
            stateStore.clear()
        } catch (error: Exception) {
            throw DataCipherException.KeyStateCorrupted(error)
        }
    }

    /** 転送用に、Prefs に保存されているラップ済み鍵状態をコピーで返す。未設定なら `null`。 */
    fun exportPersistedFieldCipherState(): ExportedVaultCryptoState? {
        val w = stateStore.load() ?: return null
        return ExportedVaultCryptoState(
            salt = w.salt.copyOf(),
            iv = w.iv.copyOf(),
            wrappedSessionKey = w.wrappedKey.copyOf()
        )
    }

    /** 他端末から取り込んだラップ状態を Prefs に保存する（既存セッションは破棄）。 */
    fun importPersistedFieldCipherState(state: ExportedVaultCryptoState) {
        lockSession()
        stateStore.save(
            WrappedSessionKeyState(
                salt = state.salt.copyOf(),
                iv = state.iv.copyOf(),
                wrappedKey = state.wrappedSessionKey.copyOf()
            )
        )
    }

    /**
     * マスターパスワード変更に合わせてラップ済みセッション鍵を付け替え、メモリのセッション鍵も更新する。
     */
    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val currentPasswordBytes = currentPassword.toByteArray(StandardCharsets.UTF_8)
        val newPasswordBytes = newPassword.toByteArray(StandardCharsets.UTF_8)
        val nextState = try {
            val persistedState = stateStore.load()
            if (persistedState != null) {
                sessionKeyProtector.rewrap(
                    currentPassword = currentPasswordBytes,
                    newPassword = newPasswordBytes,
                    wrappedState = persistedState
                )
            } else {
                sessionKeyProtector.create(newPasswordBytes)
            }
        } catch (error: Exception) {
            throw DataCipherException.KeyStateCorrupted(error)
        } finally {
            currentPasswordBytes.fill(0)
            newPasswordBytes.fill(0)
        }

        try {
            stateStore.save(nextState.wrappedState)
        } catch (error: Exception) {
            nextState.sessionKey.fill(0)
            throw DataCipherException.KeyStateCorrupted(error)
        }

        replaceSessionKey(nextState.sessionKey)
    }

    /** 現在のセッション鍵で AES-GCM 暗号化し、(暗号文, IV) を返す。 */
    fun encrypt(data: String): Pair<ByteArray, ByteArray> {
        val sessionKeyCopy = requireSessionKeyCopy()
        val plainBytes = data.toByteArray(StandardCharsets.UTF_8)
        return try {
            val cipher = Cipher.getInstance(AesTransformation)
            cipher.init(Cipher.ENCRYPT_MODE, sessionKeyCopy.toSecretKey())
            Pair(cipher.doFinal(plainBytes), cipher.iv)
        } catch (error: Exception) {
            throw DataCipherException.EncryptionFailed(error)
        } finally {
            plainBytes.fill(0)
            sessionKeyCopy.fill(0)
        }
    }

    /**
     * セッション鍵で復号する。旧 Keystore 鍵でしか読めない場合は [DataCipherDecryptionResult.requiresMigration] が `true`。
     */
    fun decrypt(
        encryptedData: ByteArray,
        iv: ByteArray
    ): DataCipherDecryptionResult {
        val sessionKeyCopy = requireSessionKeyCopy()
        return try {
            runCatching {
                decryptWithSessionKey(
                    encryptedData = encryptedData,
                    iv = iv,
                    sessionKey = sessionKeyCopy
                )
            }.fold(
                onSuccess = { plainText ->
                    DataCipherDecryptionResult(
                        plainText = plainText,
                        requiresMigration = false
                    )
                },
                onFailure = { initialError ->
                    decryptWithLegacyKey(
                        encryptedData = encryptedData,
                        iv = iv,
                        initialError = initialError
                    )
                }
            )
        } finally {
            sessionKeyCopy.fill(0)
        }
    }

    private fun decryptWithSessionKey(
        encryptedData: ByteArray,
        iv: ByteArray,
        sessionKey: ByteArray
    ): String {
        val cipher = Cipher.getInstance(AesTransformation)
        cipher.init(
            Cipher.DECRYPT_MODE,
            sessionKey.toSecretKey(),
            GCMParameterSpec(GcmTagLengthBits, iv)
        )
        val decryptedBytes = cipher.doFinal(encryptedData)
        return try {
            String(decryptedBytes, StandardCharsets.UTF_8)
        } finally {
            decryptedBytes.fill(0)
        }
    }

    private fun decryptWithLegacyKey(
        encryptedData: ByteArray,
        iv: ByteArray,
        initialError: Throwable
    ): DataCipherDecryptionResult {
        val legacyKey = legacySecretKeyOrNull()
            ?: throw DataCipherException.DecryptionFailed(initialError)

        return try {
            val cipher = Cipher.getInstance(AesTransformation)
            cipher.init(
                Cipher.DECRYPT_MODE,
                legacyKey,
                GCMParameterSpec(GcmTagLengthBits, iv)
            )
            val decryptedBytes = cipher.doFinal(encryptedData)
            try {
                DataCipherDecryptionResult(
                    plainText = String(decryptedBytes, StandardCharsets.UTF_8),
                    requiresMigration = true
                )
            } finally {
                decryptedBytes.fill(0)
            }
        } catch (legacyError: Exception) {
            throw DataCipherException.DecryptionFailed(
                cause = legacyError.apply { addSuppressed(initialError) }
            )
        }
    }

    private fun replaceSessionKey(nextSessionKey: ByteArray) {
        synchronized(sessionKeyLock) {
            sessionKey?.fill(0)
            sessionKey = nextSessionKey
        }
    }

    private fun requireSessionKeyCopy(): ByteArray {
        return synchronized(sessionKeyLock) {
            sessionKey?.copyOf() ?: throw DataCipherException.SessionLocked
        }
    }

    private fun ByteArray.toSecretKey(): SecretKey {
        return SecretKeySpec(this, AesAlgorithm)
    }

    private fun legacySecretKeyOrNull(): SecretKey? {
        return keyStore.getKey(LegacyKeyAlias, null) as? SecretKey
    }

    private companion object {
        const val AndroidKeyStore = "AndroidKeyStore"
        const val AesAlgorithm = "AES"
        const val AesTransformation = "AES/GCM/NoPadding"
        const val GcmTagLengthBits = 128
        const val LegacyKeyAlias = "data_encryption_key"
    }
}

/** 復号結果と、旧キーで読んだため再暗号化が必要かどうか。 */
data class DataCipherDecryptionResult(
    val plainText: String,
    val requiresMigration: Boolean
)

/** [DataCipher] のセッション・永続状態・暗号処理の失敗。 */
sealed class DataCipherException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    /** [unlockSession] 前に暗号操作をした。 */
    data object SessionLocked : DataCipherException("Data cipher session is locked")

    /** 保存済みラップ状態の読み取り・形式が壊れている。 */
    class KeyStateCorrupted(cause: Throwable? = null) :
        DataCipherException("Data cipher key state is corrupted", cause)

    /** 暗号化処理が失敗した。 */
    class EncryptionFailed(cause: Throwable? = null) :
        DataCipherException("Failed to encrypt data", cause)

    /** 復号処理が失敗した。 */
    class DecryptionFailed(cause: Throwable? = null) :
        DataCipherException("Failed to decrypt data", cause)
}

/** [DataCipher] のラップ済みセッション鍵を SharedPreferences に保存する。 */
private class DataCipherStateStore(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE
    )

    fun load(): WrappedSessionKeyState? {
        val encodedSalt = sharedPreferences.getString(KeySalt, null)
        val encodedIv = sharedPreferences.getString(KeyIv, null)
        val encodedWrappedKey = sharedPreferences.getString(KeyWrappedSessionKey, null)

        if (encodedSalt == null && encodedIv == null && encodedWrappedKey == null) {
            return null
        }

        requireNotNull(encodedSalt) { "Missing data cipher salt" }
        requireNotNull(encodedIv) { "Missing data cipher IV" }
        requireNotNull(encodedWrappedKey) { "Missing wrapped data cipher key" }

        return WrappedSessionKeyState(
            salt = Base64.getDecoder().decode(encodedSalt),
            iv = Base64.getDecoder().decode(encodedIv),
            wrappedKey = Base64.getDecoder().decode(encodedWrappedKey)
        )
    }

    fun save(state: WrappedSessionKeyState) {
        val didCommit = sharedPreferences.edit()
            .putString(KeySalt, Base64.getEncoder().encodeToString(state.salt))
            .putString(KeyIv, Base64.getEncoder().encodeToString(state.iv))
            .putString(
                KeyWrappedSessionKey,
                Base64.getEncoder().encodeToString(state.wrappedKey)
            )
            .commit()
        check(didCommit) { "Failed to persist data cipher state" }
    }

    fun clear() {
        val didCommit = sharedPreferences.edit()
            .remove(KeySalt)
            .remove(KeyIv)
            .remove(KeyWrappedSessionKey)
            .commit()
        check(didCommit) { "Failed to clear data cipher state" }
    }

    private companion object {
        const val PreferencesName = "data_cipher_state"
        const val KeySalt = "wrapped_session_key_salt"
        const val KeyIv = "wrapped_session_key_iv"
        const val KeyWrappedSessionKey = "wrapped_session_key_payload"
    }
}
