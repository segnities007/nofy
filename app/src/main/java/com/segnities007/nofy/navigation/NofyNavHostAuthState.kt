package com.segnities007.nofy.navigation

import com.segnities007.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** [AuthRepository] の登録済みフラグとロック状態のスナップショット。 */
internal data class AuthNavigationState(
    val isRegistered: Boolean,
    val isLocked: Boolean
)

/** 登録状態とロック状態をまとめて購読するフロー（ナビゲーションゲート用）。 */
internal fun AuthRepository.observeAuthNavigationState(): Flow<AuthNavigationState> {
    return combine(isRegistered(), isLocked()) { isRegistered, isLocked ->
        AuthNavigationState(
            isRegistered = isRegistered,
            isLocked = isLocked
        )
    }
}
