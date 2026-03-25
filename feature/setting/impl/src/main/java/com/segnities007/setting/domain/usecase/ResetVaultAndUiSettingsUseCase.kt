package com.segnities007.setting.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.settings.UiSettingsRepository

/**
 * 認証メタデータと永続データを初期化し、続けて UI 設定を既定値へ戻す。
 */
internal class ResetVaultAndUiSettingsUseCase(
    private val authRepository: AuthRepository,
    private val uiSettingsRepository: UiSettingsRepository
) {
    suspend operator fun invoke(currentPassword: String): Result<Unit> {
        val authResetResult = authRepository.reset(currentPassword)
        if (authResetResult.isFailure) {
            return authResetResult
        }
        return runCatching { uiSettingsRepository.reset() }
    }
}
