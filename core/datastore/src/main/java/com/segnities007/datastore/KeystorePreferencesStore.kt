package com.segnities007.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.segnities007.crypto.KeystoreAesKeyFactory
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore 鍵で各 entry を暗号化する preferences store。
 */
class KeystorePreferencesStore(
    context: Context
) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun putString(key: String, value: String) {
        putEncodedValue(key, SecurePreferencesValueCodec.encodeString(value))
    }

    fun getString(key: String): String? {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeString)
    }

    fun putBoolean(key: String, value: Boolean) {
        putEncodedValue(key, SecurePreferencesValueCodec.encodeBoolean(value))
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeBoolean) ?: defaultValue
    }

    fun putInt(key: String, value: Int) {
        putEncodedValue(key, SecurePreferencesValueCodec.encodeInt(value))
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeInt) ?: defaultValue
    }

    fun putLong(key: String, value: Long) {
        putEncodedValue(key, SecurePreferencesValueCodec.encodeLong(value))
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeLong) ?: defaultValue
    }

    fun putFloat(key: String, value: Float) {
        putEncodedValue(key, SecurePreferencesValueCodec.encodeFloat(value))
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeFloat) ?: defaultValue
    }

    fun remove(vararg keys: String) {
        sharedPreferences.edit().apply {
            keys.forEach { remove(storageKey(it)) }
        }.apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    private fun putEncodedValue(logicalKey: String, payload: String) {
        sharedPreferences.edit()
            .putString(storageKey(logicalKey), encrypt(payload))
            .apply()
    }

    private fun <T> getDecodedValue(
        logicalKey: String,
        decoder: (String) -> T?
    ): T? {
        val entryKey = storageKey(logicalKey)
        val stored = sharedPreferences.getString(entryKey, null) ?: return null
        val payload = try {
            decrypt(stored)
        } catch (error: Exception) {
            Log.w(TAG, "Failed to decrypt secure preference: $logicalKey", error)
            sharedPreferences.edit().remove(entryKey).apply()
            return null
        }
        return decoder(payload)
    }

    private fun storageKey(logicalKey: String): String {
        val digest = MessageDigest.getInstance(SHA_256)
        val hashedKey = digest.digest(logicalKey.toByteArray(StandardCharsets.UTF_8))
        return KEY_PREFIX + Base64.encodeToString(hashedKey, Base64.NO_WRAP)
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encodedEncrypted = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        return "$encodedIv:$encodedEncrypted"
    }

    private fun decrypt(encryptedValue: String): String {
        val parts = encryptedValue.split(':', limit = 2)
        require(parts.size == 2) { "Malformed secure preference payload" }

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), spec)
        val plainText = cipher.doFinal(encrypted)
        return String(plainText, StandardCharsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        return KeystoreAesKeyFactory.generate(KEY_ALIAS)
    }

    private companion object {
        const val TAG = "KeystorePrefsStore"
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        const val SHA_256 = "SHA-256"
        const val GCM_TAG_LENGTH_BITS = 128
        const val KEY_ALIAS = "secure_preferences_key_v2"
        const val PREFERENCES_NAME = "secure_keystore_prefs"
        const val KEY_PREFIX = "entry_"
    }
}
