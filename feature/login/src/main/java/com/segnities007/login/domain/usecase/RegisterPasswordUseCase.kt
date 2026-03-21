package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository

internal class RegisterPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): PasswordRegistrationResult {
        val result = authRepository.registerPassword(password)
        if (result.isSuccess) {
            return PasswordRegistrationResult.Success
        }

        return when (val error = result.exceptionOrNull()) {
            is AuthException.PasswordTooShort -> PasswordRegistrationResult.TooShort(error.minimumLength)
            AuthException.PasswordTooCommon -> PasswordRegistrationResult.TooCommon
            else -> PasswordRegistrationResult.Failure
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
    data class TooShort(val minimumLength: Int) : PasswordRegistrationResult
    data object TooCommon : PasswordRegistrationResult
    data object Failure : PasswordRegistrationResult
}

internal sealed interface BiometricSecretSaveResult {
    data object Success : BiometricSecretSaveResult
    data object Failure : BiometricSecretSaveResult
}
