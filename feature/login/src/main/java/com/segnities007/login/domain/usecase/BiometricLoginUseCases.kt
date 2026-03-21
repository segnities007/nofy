package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

internal class ObserveBiometricEnabledUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isBiometricEnabled()
    }
}

internal class ClearBiometricSecretUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.clearBiometricSecret()
    }
}
