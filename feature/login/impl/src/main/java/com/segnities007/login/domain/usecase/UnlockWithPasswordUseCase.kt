package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository

internal class UnlockWithPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): LoginSubmissionResult {
        return authRepository.unlock(password).toLoginSubmissionResult()
    }
}

internal class UnlockWithBiometricUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): LoginSubmissionResult {
        return authRepository.unlockWithBiometric(password).toLoginSubmissionResult()
    }
}

internal sealed interface LoginSubmissionResult {
    data object Success : LoginSubmissionResult
    data object Failure : LoginSubmissionResult
    data object UntrustedEnvironment : LoginSubmissionResult
    data class LockedOut(val remainingMillis: Long) : LoginSubmissionResult
}

private fun Result<Unit>.toLoginSubmissionResult(): LoginSubmissionResult {
    if (isSuccess) {
        return LoginSubmissionResult.Success
    }

    val error = exceptionOrNull()
    return when (error) {
        is AuthException.LockedOut -> LoginSubmissionResult.LockedOut(error.remainingMillis)
        AuthException.UntrustedEnvironment -> LoginSubmissionResult.UntrustedEnvironment
        else -> LoginSubmissionResult.Failure
    }
}
