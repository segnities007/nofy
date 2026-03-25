package com.segnities007.datastore

/** パスワードハッシュ・生体シークレット・ロックアウト状態を [KeystorePreferencesStore] に保存する。 */
class AuthLocalDataSource(
    private val store: KeystorePreferencesStore
) {
    fun savePasswordHash(hash: String) {
        store.putString(KEY_PASSWORD_HASH, hash, commitSynchronously = true)
    }

    fun getPasswordHash(): String? = store.getString(KEY_PASSWORD_HASH)

    fun saveBiometricSecret(secret: String, iv: String) {
        store.update(commitSynchronously = true) {
            putString(KEY_BIOMETRIC_SECRET, secret)
            putString(KEY_BIOMETRIC_IV, iv)
            putBoolean(KEY_BIOMETRIC_ENABLED, true)
        }
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

    fun hasBiometricSecret(): Boolean {
        return getBiometricSecret() != null
    }

    fun isBiometricEnabled(): Boolean = store.getBoolean(KEY_BIOMETRIC_ENABLED)

    fun setBiometricEnabled(enabled: Boolean) {
        store.putBoolean(KEY_BIOMETRIC_ENABLED, enabled, commitSynchronously = true)
    }

    fun clearBiometricSecret() {
        store.update(commitSynchronously = true) {
            remove(KEY_BIOMETRIC_SECRET, KEY_BIOMETRIC_IV)
            putBoolean(KEY_BIOMETRIC_ENABLED, false)
        }
    }

    fun clearAuthState() {
        store.update(commitSynchronously = true) {
            remove(
                KEY_PASSWORD_HASH,
                KEY_BIOMETRIC_SECRET,
                KEY_BIOMETRIC_IV,
                KEY_BIOMETRIC_ENABLED,
                KEY_FAILED_PASSWORD_ATTEMPTS,
                KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS
            )
        }
    }

    fun getFailedPasswordAttempts(): Int = store.getInt(KEY_FAILED_PASSWORD_ATTEMPTS)

    fun savePasswordEntryState(
        failedAttempts: Int,
        lockoutUntilMillis: Long
    ) {
        store.update(commitSynchronously = true) {
            putInt(KEY_FAILED_PASSWORD_ATTEMPTS, failedAttempts)
            putLong(KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS, lockoutUntilMillis)
        }
    }

    fun getPasswordLockoutUntilMillis(): Long = store.getLong(KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS)

    fun clearPasswordAttemptState() {
        store.update(commitSynchronously = true) {
            remove(KEY_FAILED_PASSWORD_ATTEMPTS, KEY_PASSWORD_LOCKOUT_UNTIL_MILLIS)
        }
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
