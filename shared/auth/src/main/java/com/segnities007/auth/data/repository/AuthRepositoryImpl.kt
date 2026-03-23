package com.segnities007.auth.data.repository

import android.util.Base64
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.policy.PasswordPolicy
import com.segnities007.auth.domain.policy.PasswordPolicyViolation
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.auth.domain.security.SensitiveOperationBlockedException
import com.segnities007.auth.domain.security.SensitiveOperationGuard
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.PasswordHasher
import com.segnities007.database.SecureDatabaseController
import com.segnities007.datastore.AuthLocalDataSource
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 認証・ロック・生体・SQLCipher／[DataCipher] のオーケストレーションを担う [AuthRepository] 実装。
 */
class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val passwordHasher: PasswordHasher,
    private val databaseController: SecureDatabaseController,
    private val dataCipher: DataCipher,
    private val sensitiveOperationGuard: SensitiveOperationGuard,
    private val nowMillisProvider: () -> Long = System::currentTimeMillis
) : AuthRepository {
    private val passwordEntryGate = AuthPasswordEntryGate(
        authLocalDataSource = authLocalDataSource,
        nowMillisProvider = nowMillisProvider
    )

    private val registeredState = MutableStateFlow(authLocalDataSource.getPasswordHash() != null)
    private val biometricEnabledState = MutableStateFlow(authLocalDataSource.isBiometricEnabled())
    private val lockedState = MutableStateFlow(true)

    override fun isRegistered(): Flow<Boolean> {
        return registeredState.asStateFlow()
    }

    override fun isBiometricEnabled(): Flow<Boolean> {
        return biometricEnabledState.asStateFlow()
    }

    override fun isLocked(): Flow<Boolean> {
        return lockedState.asStateFlow()
    }

    override suspend fun verifyPassword(password: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            verifyPasswordForPasswordEntry(password)
            clearPasswordAttemptState()
        }.recoverCatching { passwordEntryGate.recoverFromPasswordEntryFailure(it) }
    }

    override suspend fun lock(): Result<Unit> {
        return runCatching {
            dataCipher.lockSession()
            databaseController.lock()
            lockedState.value = true
        }
    }

    override suspend fun registerPassword(password: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            ensureNotRegistered()
            ensurePasswordPolicySatisfied(password)
            storePasswordHash(password)
            unlockDatabase(password)
            dataCipher.unlockSession(password)
            clearPasswordAttemptState()
            lockedState.value = false
        }
    }

    override suspend fun unlock(password: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            verifyPasswordForPasswordEntry(password)
            unlockDatabase(password)
            dataCipher.unlockSession(password)
            clearPasswordAttemptState()
            lockedState.value = false
        }.recoverCatching { passwordEntryGate.recoverFromPasswordEntryFailure(it) }
    }

    override suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            ensurePasswordMatches(decryptedPassword)
            unlockDatabase(decryptedPassword)
            dataCipher.unlockSession(decryptedPassword)
            clearPasswordAttemptState()
            lockedState.value = false
        }
    }

    override suspend fun saveBiometricSecret(encryptedSecret: ByteArray, iv: ByteArray): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            val secretString = Base64.encodeToString(encryptedSecret, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            authLocalDataSource.saveBiometricSecret(secretString, ivString)
            biometricEnabledState.value = true
        }
    }

    override suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>? {
        ensureSensitiveOperationAllowed()
        val pair = authLocalDataSource.getBiometricSecret() ?: return null
        return try {
            val secret = Base64.decode(pair.first, Base64.DEFAULT)
            val iv = Base64.decode(pair.second, Base64.DEFAULT)
            secret to iv
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clearBiometricSecret(): Result<Unit> {
        return runCatching {
            authLocalDataSource.clearBiometricSecret()
            biometricEnabledState.value = false
        }
    }

    override suspend fun reset(currentPassword: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            verifyPasswordForPasswordEntry(currentPassword)
            performReset()
        }.recoverCatching { passwordEntryGate.recoverFromPasswordEntryFailure(it) }
    }

    private fun performReset() {
        dataCipher.lockSession()
        dataCipher.clearState()
        authLocalDataSource.clearAuthState()
        registeredState.value = false
        biometricEnabledState.value = false
        lockedState.value = true
        databaseController.deleteDatabaseFiles()
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        return runCatching {
            if (!enabled) {
                authLocalDataSource.clearBiometricSecret()
                biometricEnabledState.value = false
                return@runCatching
            }

            ensureSensitiveOperationAllowed()
            if (!authLocalDataSource.hasBiometricSecret()) {
                throw AuthException.BiometricNotEnrolled
            }

            authLocalDataSource.setBiometricEnabled(true)
            biometricEnabledState.value = true
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            ensurePasswordPolicySatisfied(newPassword)
            verifyPasswordForPasswordEntry(currentPassword)
            applyPasswordChange(currentPassword, newPassword)
            clearPasswordAttemptState()
        }.recoverCatching { passwordEntryGate.recoverFromPasswordEntryFailure(it) }
    }

    override suspend fun adoptImportedVault(password: String): Result<Unit> {
        return runCatching {
            ensureSensitiveOperationAllowed()
            unlockDatabase(password)
            dataCipher.unlockSession(password)
            storePasswordHash(password)
            authLocalDataSource.clearBiometricSecret()
            biometricEnabledState.value = false
            clearPasswordAttemptState()
            lockedState.value = false
        }
    }

    private fun ensureNotRegistered() {
        if (authLocalDataSource.getPasswordHash() != null) {
            throw AuthException.AlreadyRegistered
        }
    }

    private fun ensurePasswordMatches(password: String) {
        val storedHash = authLocalDataSource.getPasswordHash()
            ?: throw AuthException.NotRegistered

        if (!passwordHasher.verifyPassword(password, storedHash)) {
            throw AuthException.InvalidPassword
        }
    }

    private fun ensureSensitiveOperationAllowed() {
        try {
            sensitiveOperationGuard.ensureSensitiveOperationAllowed()
        } catch (_: SensitiveOperationBlockedException) {
            throw AuthException.UntrustedEnvironment
        }
    }

    private fun storePasswordHash(password: String) {
        val hash = passwordHasher.hashPassword(password)
        authLocalDataSource.savePasswordHash(hash)
        registeredState.value = true
    }

    private fun ensurePasswordPolicySatisfied(password: String) {
        when (val violation = PasswordPolicy.violationOrNull(password)) {
            null -> Unit
            is PasswordPolicyViolation.TooShort -> throw AuthException.PasswordTooShort(
                minimumLength = violation.minimumLength
            )
            PasswordPolicyViolation.TooCommon -> throw AuthException.PasswordTooCommon
        }
    }

    private fun unlockDatabase(password: String) {
        val passphrase = password.toByteArray(StandardCharsets.UTF_8)
        try {
            databaseController.unlock(passphrase)
        } finally {
            passphrase.fill(0)
        }
    }

    private fun applyPasswordChange(currentPassword: String, newPassword: String) {
        databaseController.changePassphrase(currentPassword, newPassword)
        try {
            dataCipher.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        } catch (error: Throwable) {
            rollbackDatabasePasswordChange(
                currentPassword = currentPassword,
                newPassword = newPassword,
                originalError = error
            )
        }
        storePasswordHash(newPassword)

        // Existing biometric credentials are tied to the old password and must be re-enrolled.
        authLocalDataSource.clearBiometricSecret()
        biometricEnabledState.value = false
        lockedState.value = false
    }

    private fun rollbackDatabasePasswordChange(
        currentPassword: String,
        newPassword: String,
        originalError: Throwable
    ): Nothing {
        runCatching {
            databaseController.changePassphrase(newPassword, currentPassword)
        }.onFailure(originalError::addSuppressed)
        throw originalError
    }

    private fun verifyPasswordForPasswordEntry(password: String) {
        passwordEntryGate.ensurePasswordEntryAllowed()
        ensurePasswordMatches(password)
    }

    private fun clearPasswordAttemptState() {
        passwordEntryGate.clearPasswordAttemptState()
    }
}
