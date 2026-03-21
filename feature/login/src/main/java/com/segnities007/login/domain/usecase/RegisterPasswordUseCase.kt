package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository

internal class RegisterPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): PasswordRegistrationResult {
        return if (authRepository.registerPassword(password).isSuccess) {
            PasswordRegistrationResult.Success
        } else {
            PasswordRegistrationResult.Failure
        }
    }
}

internal class SaveBiometricSecretUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        encryptedSecret: ByteArray,
        iv: ByteArray
    ): BiometricSecretSaveResult {
        return if (authRepository.saveBiometricSecret(encryptedSecret, iv).isSuccess) {
            BiometricSecretSaveResult.Success
        } else {
            BiometricSecretSaveResult.Failure
        }
    }
}

internal sealed interface PasswordRegistrationResult {
    data object Success : PasswordRegistrationResult
    data object Failure : PasswordRegistrationResult
}

internal sealed interface BiometricSecretSaveResult {
    data object Success : BiometricSecretSaveResult
    data object Failure : BiometricSecretSaveResult
}
