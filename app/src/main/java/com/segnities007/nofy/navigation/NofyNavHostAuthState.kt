package com.segnities007.nofy.navigation

import com.segnities007.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal data class AuthNavigationState(
    val isRegistered: Boolean,
    val isLocked: Boolean
)

internal fun AuthRepository.observeAuthNavigationState(): Flow<AuthNavigationState> {
    return combine(isRegistered(), isLocked()) { isRegistered, isLocked ->
        AuthNavigationState(
            isRegistered = isRegistered,
            isLocked = isLocked
        )
    }
}
