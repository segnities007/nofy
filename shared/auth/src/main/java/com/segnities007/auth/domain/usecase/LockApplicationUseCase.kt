package com.segnities007.auth.domain.usecase

import com.segnities007.auth.domain.repository.AuthRepository

/** セッションを閉じ、アプリをロック状態にする 1 操作。 */
class LockApplicationUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.lock()
}
