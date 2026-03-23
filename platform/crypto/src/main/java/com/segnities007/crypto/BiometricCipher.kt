package com.segnities007.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 生体認証と連携するための暗号化クラス。
 * Android Keystoreを使用して、生体認証成功時のみ秘密鍵にアクセスできるようにする。
 */
class BiometricCipher {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "biometric_key"

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        KeystoreAesKeyFactory.generate(keyAlias) {
            setUserAuthenticationRequired(true) // 生体認証を必須にする
            setInvalidatedByBiometricEnrollment(true) // 新しい指紋が登録されたら無効化
        }
    }

    /** 生体必須の Keystore 鍵で AES-GCM 暗号化モードの [Cipher] を返す。 */
    fun getEncryptCipher(): Cipher {
        return runCatching(::createEncryptCipher)
            .getOrElse { initialError ->
                recreateKey()
                runCatching(::createEncryptCipher)
                    .getOrElse { recoveryError ->
                        throw recoveryError.toCredentialUnavailable(initialError)
                    }
            }
    }

    /** 保存した IV で復号モードの [Cipher] を返す。 */
    fun getDecryptCipher(iv: ByteArray): Cipher {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val key = requireSecretKey()
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            cipher
        } catch (error: Exception) {
            throw error.toCredentialUnavailable()
        }
    }

    /** 平文文字列を暗号化し、(暗号文, IV) を返す。平文バッファは使用後ゼロクリアする。 */
    fun encrypt(data: String, cipher: Cipher): Pair<ByteArray, ByteArray> {
        val plainBytes = data.toByteArray(StandardCharsets.UTF_8)
        return try {
            encrypt(plainBytes, cipher)
        } finally {
            plainBytes.fill(0)
        }
    }

    /** 平文バイト列を暗号化し、(暗号文, IV) を返す。 */
    fun encrypt(data: ByteArray, cipher: Cipher): Pair<ByteArray, ByteArray> {
        return try {
            Pair(cipher.doFinal(data), cipher.iv)
        } catch (error: Exception) {
            throw error.toCredentialUnavailable()
        }
    }

    /** 暗号文を UTF-8 文字列へ復号する。復号バッファは使用後ゼロクリアする。 */
    fun decrypt(encryptedData: ByteArray, cipher: Cipher): String {
        val decryptedData = decryptToByteArray(encryptedData, cipher)
        return try {
            String(decryptedData, StandardCharsets.UTF_8)
        } finally {
            decryptedData.fill(0)
        }
    }

    /** 暗号文を生のバイト列で復号する（パスワードバイト列向け）。 */
    fun decryptToByteArray(
        encryptedData: ByteArray,
        cipher: Cipher
    ): ByteArray {
        return try {
            cipher.doFinal(encryptedData)
        } catch (error: Exception) {
            throw error.toCredentialUnavailable()
        }
    }

    private fun createEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = requireSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    private fun requireSecretKey(): SecretKey {
        ensureKeyExists()
        return keyStore.getKey(keyAlias, null) as? SecretKey
            ?: throw CredentialUnavailableException()
    }

    private fun ensureKeyExists() {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun recreateKey() {
        runCatching {
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
            }
            generateKey()
        }.getOrElse { error ->
            throw error.toCredentialUnavailable()
        }
    }

    private fun Throwable.toCredentialUnavailable(
        originalCause: Throwable? = null
    ): CredentialUnavailableException {
        return when (this) {
            is CredentialUnavailableException -> this
            is GeneralSecurityException, is IllegalArgumentException, is IllegalStateException -> {
                CredentialUnavailableException(originalCause ?: this)
            }

            else -> throw this
        }
    }

    class CredentialUnavailableException(
        cause: Throwable? = null
    ) : IllegalStateException("Biometric credential unavailable", cause)
}
