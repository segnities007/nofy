package com.segnities007.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 一般的なデータ暗号化用のクラス。
 * メモの内容などをAES-GCMで暗号化する。
 */
class DataCipher {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "data_encryption_key"
    private val sessionUnlocked = AtomicBoolean(false)

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        KeystoreAesKeyFactory.generate(keyAlias) {
            setUserAuthenticationRequired(false) // シームレスな保存のために認証は不要にする
        }
    }

    fun unlockSession() {
        sessionUnlocked.set(true)
    }

    fun lockSession() {
        sessionUnlocked.set(false)
    }

    fun encrypt(data: String): Pair<ByteArray, ByteArray> {
        requireUnlockedSession()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Pair(encryptedData, cipher.iv)
    }

    fun decrypt(encryptedData: ByteArray, iv: ByteArray): String {
        requireUnlockedSession()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }

    private fun requireUnlockedSession() {
        if (!sessionUnlocked.get()) {
            throw DataCipherException.SessionLocked
        }
    }
}

sealed class DataCipherException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    data object SessionLocked : DataCipherException("Data cipher session is locked")
}
