package com.segnities007.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
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

    fun putString(
        key: String,
        value: String,
        commitSynchronously: Boolean = false
    ) {
        update(commitSynchronously) {
            putString(key, value)
        }
    }

    fun getString(key: String): String? {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeString)
    }

    fun putBoolean(
        key: String,
        value: Boolean,
        commitSynchronously: Boolean = false
    ) {
        update(commitSynchronously) {
            putBoolean(key, value)
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeBoolean) ?: defaultValue
    }

    fun putInt(
        key: String,
        value: Int,
        commitSynchronously: Boolean = false
    ) {
        update(commitSynchronously) {
            putInt(key, value)
        }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeInt) ?: defaultValue
    }

    fun putLong(
        key: String,
        value: Long,
        commitSynchronously: Boolean = false
    ) {
        update(commitSynchronously) {
            putLong(key, value)
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeLong) ?: defaultValue
    }

    fun putFloat(
        key: String,
        value: Float,
        commitSynchronously: Boolean = false
    ) {
        update(commitSynchronously) {
            putFloat(key, value)
        }
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getDecodedValue(key, SecurePreferencesValueCodec::decodeFloat) ?: defaultValue
    }

    fun remove(
        vararg keys: String,
        commitSynchronously: Boolean = false
    ) {
        update(commitSynchronously) {
            remove(*keys)
        }
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    internal fun update(
        commitSynchronously: Boolean = false,
        mutation: SecurePreferencesMutation.() -> Unit
    ) {
        val editor = sharedPreferences.edit()
        SecurePreferencesMutation(editor).mutation()
        persist(editor, commitSynchronously)
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

    private fun persist(
        editor: SharedPreferences.Editor,
        commitSynchronously: Boolean
    ) {
        if (!commitSynchronously) {
            editor.apply()
            return
        }

        check(editor.commit()) { "Failed to persist secure preferences" }
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val plainTextBytes = plainText.toByteArray(StandardCharsets.UTF_8)
        val encrypted = try {
            cipher.doFinal(plainTextBytes)
        } finally {
            plainTextBytes.fill(0)
        }
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
        return try {
            String(plainText, StandardCharsets.UTF_8)
        } finally {
            plainText.fill(0)
        }
    }

    private fun secretKey(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        return KeystoreAesKeyFactory.generate(KEY_ALIAS)
    }

    private companion object {
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        const val SHA_256 = "SHA-256"
        const val GCM_TAG_LENGTH_BITS = 128
        const val KEY_ALIAS = "secure_preferences_key_v2"
        const val PREFERENCES_NAME = "secure_keystore_prefs"
        const val KEY_PREFIX = "entry_"
    }

    internal inner class SecurePreferencesMutation(
        private val editor: SharedPreferences.Editor
    ) {
        fun putString(key: String, value: String) {
            putEncodedValue(key, SecurePreferencesValueCodec.encodeString(value))
        }

        fun putBoolean(key: String, value: Boolean) {
            putEncodedValue(key, SecurePreferencesValueCodec.encodeBoolean(value))
        }

        fun putInt(key: String, value: Int) {
            putEncodedValue(key, SecurePreferencesValueCodec.encodeInt(value))
        }

        fun putLong(key: String, value: Long) {
            putEncodedValue(key, SecurePreferencesValueCodec.encodeLong(value))
        }

        fun putFloat(key: String, value: Float) {
            putEncodedValue(key, SecurePreferencesValueCodec.encodeFloat(value))
        }

        fun remove(vararg keys: String) {
            keys.forEach { key ->
                editor.remove(storageKey(key))
            }
        }

        private fun putEncodedValue(logicalKey: String, payload: String) {
            editor.putString(storageKey(logicalKey), encrypt(payload))
        }
    }
}
