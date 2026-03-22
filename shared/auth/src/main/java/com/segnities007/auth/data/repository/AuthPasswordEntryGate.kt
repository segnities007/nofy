package com.segnities007.auth.data.repository

import com.segnities007.auth.domain.error.AuthException
import com.segnities007.datastore.AuthLocalDataSource

/**
 * パスワード入力の lockout / 失敗回数を [AuthLocalDataSource] に閉じ込める。
 * [AuthRepositoryImpl] の認証フローから手続きを分離する。
 */
internal class AuthPasswordEntryGate(
    private val authLocalDataSource: AuthLocalDataSource,
    private val nowMillisProvider: () -> Long
) {
    fun ensurePasswordEntryAllowed() {
        val state = normalizedPasswordEntryState()
        val remainingLockout = PasswordEntryPolicy.activeLockoutRemainingMillis(
            state = state,
            nowMillis = nowMillis()
        ) ?: return
        throw AuthException.LockedOut(remainingLockout)
    }

    fun recoverFromPasswordEntryFailure(error: Throwable): Nothing {
        throw when (error) {
            is AuthException.InvalidPassword -> recordFailedPasswordAttempt()
            else -> error
        }
    }

    fun clearPasswordAttemptState() {
        persistPasswordEntryState(PasswordEntryPolicy.cleared())
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
        authLocalDataSource.savePasswordEntryState(
            failedAttempts = state.failedAttempts,
            lockoutUntilMillis = state.lockoutEndsAtMillis
        )
    }

    private fun nowMillis(): Long = nowMillisProvider()
}
