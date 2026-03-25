package com.segnities007.setting.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository

/** 生体ログインの利用可否フラグを更新する。 */
internal class SetBiometricLoginEnabledUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> =
        authRepository.setBiometricEnabled(enabled)
}
