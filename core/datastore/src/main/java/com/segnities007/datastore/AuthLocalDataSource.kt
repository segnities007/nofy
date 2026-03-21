package com.segnities007.datastore

class AuthLocalDataSource(
    private val store: KeystorePreferencesStore
) {
    fun savePasswordHash(hash: String) {
        store.putString(KEY_PASSWORD_HASH, hash)
    }

    fun getPasswordHash(): String? = store.getString(KEY_PASSWORD_HASH)

    fun saveBiometricSecret(secret: String, iv: String) {
        store.putString(KEY_BIOMETRIC_SECRET, secret)
        store.putString(KEY_BIOMETRIC_IV, iv)
        store.putBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun getBiometricSecret(): Pair<String, String>? {
        val secret = store.getString(KEY_BIOMETRIC_SECRET)
        val iv = store.getString(KEY_BIOMETRIC_IV)
        return if (secret != null && iv != null) {
            secret to iv
        } else {
            null
        }
    }

    fun isBiometricEnabled(): Boolean = store.getBoolean(KEY_BIOMETRIC_ENABLED)

    fun setBiometricEnabled(enabled: Boolean) {
        store.putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
    }

    fun clearBiometricSecret() {
        store.remove(KEY_BIOMETRIC_SECRET, KEY_BIOMETRIC_IV)
        store.putBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun clearAuthState() {
        store.remove(
            KEY_PASSWORD_HASH,
            KEY_BIOMETRIC_SECRET,
            KEY_BIOMETRIC_IV,
            KEY_BIOMETRIC_ENABLED,
            KEY_FAILED_PASSWORD_ATTEMPTS,
            KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS
        )
    }

    fun saveFailedPasswordAttempts(count: Int) {
        store.putInt(KEY_FAILED_PASSWORD_ATTEMPTS, count)
    }

    fun getFailedPasswordAttempts(): Int = store.getInt(KEY_FAILED_PASSWORD_ATTEMPTS)

    fun savePasswordLockoutUntilMillis(untilMillis: Long) {
        store.putLong(KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS, untilMillis)
    }

    fun getPasswordLockoutUntilMillis(): Long = store.getLong(KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS)

    fun clearPasswordAttemptState() {
        store.remove(KEY_FAILED_PASSWORD_ATTEMPTS, KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS)
    }

    private companion object {
        const val KEY_PASSWORD_HASH = "password_hash"
        const val KEY_BIOMETRIC_SECRET = "biometric_secret"
        const val KEY_BIOMETRIC_IV = "biometric_iv"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_FAILED_PASSWORD_ATTEMPTS = "failed_password_attempts"
        const val KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS = "password_lockout_until_millis"
    }
}
