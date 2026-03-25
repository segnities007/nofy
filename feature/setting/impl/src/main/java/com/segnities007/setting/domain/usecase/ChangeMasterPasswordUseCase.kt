package com.segnities007.setting.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository

/** 現在のマスターを検証したうえで新パスワードへ変更する。 */
internal class ChangeMasterPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = authRepository.changePassword(currentPassword, newPassword)
}
