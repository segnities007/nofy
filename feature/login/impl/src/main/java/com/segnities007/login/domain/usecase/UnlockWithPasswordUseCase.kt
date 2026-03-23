package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository

/** マスターパスワード入力によるロック解除。 */
internal class UnlockWithPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): LoginSubmissionResult {
        return authRepository.unlock(password).toLoginSubmissionResult()
    }
}

/** 生体で復号した平文パスワードによるロック解除（中身は [AuthRepository.unlockWithBiometric]）。 */
internal class UnlockWithBiometricUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): LoginSubmissionResult {
        return authRepository.unlockWithBiometric(password).toLoginSubmissionResult()
    }
}

/**
 * パスワードまたは生体経由のロック解除（ログイン）試行の結果。
 */
internal sealed interface LoginSubmissionResult {
    /** ロック解除に成功した。 */
    data object Success : LoginSubmissionResult

    /** パスワード不一致など、通常の失敗。 */
    data object Failure : LoginSubmissionResult

    /** 端末が信頼できず操作を拒否された。 */
    data object UntrustedEnvironment : LoginSubmissionResult

    /** 短時間の試行制限中。残り [remainingMillis] ms。 */
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
