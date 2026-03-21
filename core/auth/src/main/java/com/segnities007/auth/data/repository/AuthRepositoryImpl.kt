package com.segnities007.auth.data.repository

import android.util.Base64
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.policy.PasswordPolicy
import com.segnities007.auth.domain.policy.PasswordPolicyViolation
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.PasswordHasher
import com.segnities007.database.SecureDatabaseController
import com.segnities007.datastore.AuthLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val passwordHasher: PasswordHasher,
    private val databaseController: SecureDatabaseController,
    private val dataCipher: DataCipher,
    private val nowMillisProvider: () -> Long = System::currentTimeMillis
) : AuthRepository {
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

    override suspend fun lock(): Result<Unit> {
        return runCatching {
            dataCipher.lockSession()
            databaseController.lock()
            lockedState.value = true
        }
    }

    override suspend fun registerPassword(password: String): Result<Unit> {
        return runCatching {
            ensureNotRegistered()
            ensurePasswordPolicySatisfied(password)
            storePasswordHash(password)
            unlockDatabase(password)
            dataCipher.unlockSession()
            clearPasswordAttemptState()
            lockedState.value = false
        }
    }

    override suspend fun unlock(password: String): Result<Unit> {
        return runCatching {
            verifyPasswordForPasswordEntry(password)
            unlockDatabase(password)
            dataCipher.unlockSession()
            clearPasswordAttemptState()
            lockedState.value = false
        }.recoverCatching(::recoverFromPasswordEntryFailure)
    }

    override suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit> {
        return runCatching {
            ensurePasswordMatches(decryptedPassword)
            unlockDatabase(decryptedPassword)
            dataCipher.unlockSession()
            clearPasswordAttemptState()
            lockedState.value = false
        }
    }

    override suspend fun saveBiometricSecret(encryptedSecret: ByteArray, iv: ByteArray): Result<Unit> {
        return runCatching {
            val secretString = Base64.encodeToString(encryptedSecret, Base64.DEFAULT)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            authLocalDataSource.saveBiometricSecret(secretString, ivString)
            biometricEnabledState.value = true
        }
    }

    override suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>? {
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
            verifyPasswordForPasswordEntry(currentPassword)
            performReset()
        }.recoverCatching(::recoverFromPasswordEntryFailure)
    }

    private fun performReset() {
            dataCipher.lockSession()
            authLocalDataSource.clearAuthState()
            registeredState.value = false
            biometricEnabledState.value = false
            lockedState.value = true
            databaseController.deleteDatabaseFiles()
    }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> {
        authLocalDataSource.setBiometricEnabled(enabled)
        biometricEnabledState.value = enabled
        return Result.success(Unit)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return runCatching {
            ensurePasswordPolicySatisfied(newPassword)
            verifyPasswordForPasswordEntry(currentPassword)
            applyPasswordChange(currentPassword, newPassword)
            clearPasswordAttemptState()
        }.recoverCatching(::recoverFromPasswordEntryFailure)
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
        databaseController.unlock(password.toByteArray())
    }

    private fun applyPasswordChange(currentPassword: String, newPassword: String) {
        databaseController.changePassphrase(currentPassword, newPassword)
        dataCipher.unlockSession()
        storePasswordHash(newPassword)

        // Existing biometric credentials are tied to the old password and must be re-enrolled.
        authLocalDataSource.clearBiometricSecret()
        biometricEnabledState.value = false
        lockedState.value = false
    }

    private fun verifyPasswordForPasswordEntry(password: String) {
        ensurePasswordEntryAllowed()
        ensurePasswordMatches(password)
    }

    private fun ensurePasswordEntryAllowed() {
        val state = normalizedPasswordEntryState()
        val remainingLockout = PasswordEntryPolicy.activeLockoutRemainingMillis(
            state = state,
            nowMillis = nowMillis()
        ) ?: return
        throw AuthException.LockedOut(remainingLockout)
    }

    private fun recoverFromPasswordEntryFailure(error: Throwable) {
        throw when (error) {
            is AuthException.InvalidPassword -> recordFailedPasswordAttempt()
            else -> error
        }
    }

    private fun recordFailedPasswordAttempt(): AuthException {
        val updatedState = PasswordEntryPolicy.applyFailure(
            state = normalizedPasswordEntryState(),
            nowMillis = nowMillis()
        )
        persistPasswordEntryState(updatedState)
        val remainingLockout = PasswordEntryPolicy.activeLockoutRemainingMillis(
            state = updatedState,
            nowMillis = nowMillis()
        )
        return if (remainingLockout != null) {
            AuthException.LockedOut(remainingLockout)
        } else {
            AuthException.InvalidPassword
        }
    }

    private fun clearPasswordAttemptState() {
        persistPasswordEntryState(PasswordEntryPolicy.cleared())
    }

    private fun normalizedPasswordEntryState(): PasswordEntryState {
        val currentState = currentPasswordEntryState()
        val normalizedState = PasswordEntryPolicy.normalize(
            state = currentState,
            nowMillis = nowMillis()
        )
        if (normalizedState != currentState) {
            persistPasswordEntryState(normalizedState)
        }
        return normalizedState
    }

    private fun currentPasswordEntryState(): PasswordEntryState {
        return PasswordEntryState(
            failedAttempts = authLocalDataSource.getFailedPasswordAttempts(),
            lockoutEndsAtMillis = authLocalDataSource.getPasswordLockoutUntilMillis()
        )
    }

    private fun persistPasswordEntryState(state: PasswordEntryState) {
        authLocalDataSource.saveFailedPasswordAttempts(state.failedAttempts)
        authLocalDataSource.savePasswordLockoutUntilMillis(state.lockoutEndsAtMillis)
    }

    private fun nowMillis(): Long = nowMillisProvider()
}
