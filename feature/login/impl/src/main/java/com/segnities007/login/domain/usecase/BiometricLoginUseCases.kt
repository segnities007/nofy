package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/** 生体ログイン有効フラグを [AuthRepository] から購読する薄いユースケース。 */
internal class ObserveBiometricEnabledUseCase(
    private val authRepository: AuthRepository
) {
    /** 生体ログインが有効かどうかのホットストリーム。 */
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isBiometricEnabled()
    }
}

/** 保存済み生体シークレットを削除し、生体ログインを無効化する。 */
internal class ClearBiometricSecretUseCase(
    private val authRepository: AuthRepository
) {
    /** 成功時は生体でログインできなくなる。 */
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.clearBiometricSecret()
    }
}
