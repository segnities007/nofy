package com.segnities007.login.domain.usecase

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
}

private fun Result<Unit>.toLoginSubmissionResult(): LoginSubmissionResult {
    return if (isSuccess) {
        LoginSubmissionResult.Success
    } else {
        LoginSubmissionResult.Failure
    }
}
