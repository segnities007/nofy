package com.segnities007.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Android Keystoreで保護されたセキュアストレージ。
 */
class SecureAuthStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePasswordHash(hash: String) {
        sharedPrefs.edit().putString(KEY_PASSWORD_HASH, hash).apply()
    }

    fun getPasswordHash(): String? = sharedPrefs.getString(KEY_PASSWORD_HASH, null)

    fun saveBiometricSecret(secret: String, iv: String) {
        sharedPrefs.edit()
            .putString(KEY_BIOMETRIC_SECRET, secret)
            .putString(KEY_BIOMETRIC_IV, iv)
            .putBoolean(KEY_BIOMETRIC_ENABLED, true)
            .apply()
    }

    fun getBiometricSecret(): Pair<String, String>? {
        val secret = sharedPrefs.getString(KEY_BIOMETRIC_SECRET, null)
        val iv = sharedPrefs.getString(KEY_BIOMETRIC_IV, null)
        return if (secret != null && iv != null) secret to iv else null
    }

    fun isBiometricEnabled(): Boolean = sharedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun clearBiometricSecret() {
        sharedPrefs.edit()
            .remove(KEY_BIOMETRIC_SECRET)
            .remove(KEY_BIOMETRIC_IV)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun saveThemeMode(themeMode: String) {
        sharedPrefs.edit().putString(KEY_THEME_MODE, themeMode).apply()
    }

    fun getThemeMode(): String? = sharedPrefs.getString(KEY_THEME_MODE, null)

    fun saveFontScale(fontScale: Float) {
        sharedPrefs.edit().putFloat(KEY_FONT_SCALE, fontScale).apply()
    }

    fun getFontScale(): Float = sharedPrefs.getFloat(KEY_FONT_SCALE, 1f)

    fun clear() = sharedPrefs.edit().clear().apply()

    companion object {
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_BIOMETRIC_SECRET = "biometric_secret"
        private const val KEY_BIOMETRIC_IV = "biometric_iv"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_FONT_SCALE = "font_scale"
    }
}
