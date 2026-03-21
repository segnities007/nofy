package com.segnities007.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
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
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true) // 生体認証を必須にする
            .setInvalidatedByBiometricEnrollment(true) // 新しい指紋が登録されたら無効化
            .build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    fun getEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    fun getDecryptCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher
    }

    fun encrypt(data: String, cipher: Cipher): Pair<ByteArray, ByteArray> {
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Pair(encryptedData, cipher.iv)
    }

    fun decrypt(encryptedData: ByteArray, cipher: Cipher): String {
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }
}
