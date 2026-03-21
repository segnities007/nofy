package com.segnities007.auth.data.repository

import android.util.Base64
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.crypto.PasswordHasher
import com.segnities007.database.DatabaseProvider
import com.segnities007.datastore.SecureAuthStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val secureAuthStorage: SecureAuthStorage,
    private val passwordHasher: PasswordHasher,
    private val databaseProvider: DatabaseProvider
) : AuthRepository {
    private val registeredState = MutableStateFlow(secureAuthStorage.getPasswordHash() != null)
    private val biometricEnabledState = MutableStateFlow(secureAuthStorage.isBiometricEnabled())

    override fun isRegistered(): Flow<Boolean> {
        return registeredState.asStateFlow()
    }

    override fun isBiometricEnabled(): Flow<Boolean> {
        return biometricEnabledState.asStateFlow()
    }

    override suspend fun lock(): Result<Unit> {
        return runCatching {
            databaseProvider.lock()
        }
    }

    override suspend fun registerPassword(password: String): Result<Unit> {
        return runCatching {
            ensureNotRegistered()
            storePasswordHash(password)
            unlockDatabase(password)
        }
    }

    override suspend fun unlock(password: String): Result<Unit> {
        return runCatching {
            ensurePasswordMatches(password)
            unlockDatabase(password)
        }
    }

    override suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit> {
        return unlock(decryptedPassword)
    }

    override suspend fun saveBiometricSecret(encryptedSecret: ByteArray, iv: ByteArray): Result<Unit> {
        return runCatching {
            val secretString = Base64.encodeToString(encryptedSecret, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            secureAuthStorage.saveBiometricSecret(secretString, ivString)
            biometricEnabledState.value = true
        }
    }

    override suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>? {
        val pair = secureAuthStorage.getBiometricSecret() ?: return null
        return try {
            val secret = Base64.decode(pair.first, Base64.DEFAULT)
            val iv = Base64.decode(pair.second, Base64.DEFAULT)
            secret to iv
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun reset(): Result<Unit> {
        return runCatching {
            secureAuthStorage.clear()
            registeredState.value = false
            biometricEnabledState.value = false
            databaseProvider.deleteDatabaseFiles()
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        secureAuthStorage.setBiometricEnabled(enabled)
        biometricEnabledState.value = enabled
        return Result.success(Unit)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return runCatching {
            unlock(currentPassword).getOrThrow()
            applyPasswordChange(newPassword)
        }
    }

    private fun ensureNotRegistered() {
        if (secureAuthStorage.getPasswordHash() != null) {
            throw AuthException.AlreadyRegistered
        }
    }

    private fun ensurePasswordMatches(password: String) {
        val storedHash = secureAuthStorage.getPasswordHash()
            ?: throw AuthException.NotRegistered

        if (!passwordHasher.verifyPassword(password, storedHash)) {
            throw AuthException.InvalidPassword
        }
    }

    private fun storePasswordHash(password: String) {
        val hash = passwordHasher.hashPassword(password)
        secureAuthStorage.savePasswordHash(hash)
        registeredState.value = true
    }

    private fun unlockDatabase(password: String) {
        databaseProvider.unlock(password.toByteArray())
    }

    private fun applyPasswordChange(newPassword: String) {
        storePasswordHash(newPassword)

        // Existing biometric credentials are tied to the old password and must be re-enrolled.
        secureAuthStorage.clearBiometricSecret()
        biometricEnabledState.value = false

        databaseProvider.lock()
        unlockDatabase(newPassword)
    }
}
