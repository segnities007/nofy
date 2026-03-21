package com.segnities007.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
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

    fun encrypt(data: String, cipher: Cipher): Pair<ByteArray, ByteArray> {
        return try {
            val encryptedData = cipher.doFinal(data.toByteArray())
            Pair(encryptedData, cipher.iv)
        } catch (error: Exception) {
            throw error.toCredentialUnavailable()
        }
    }

    fun decrypt(encryptedData: ByteArray, cipher: Cipher): String {
        return try {
            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData)
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
