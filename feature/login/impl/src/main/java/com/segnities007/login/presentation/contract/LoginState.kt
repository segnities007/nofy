package com.segnities007.login.presentation.contract

import androidx.annotation.StringRes

data class LoginState(
    val isLoading: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val pendingUserMessage: LoginUserMessage? = null,
    val pendingNavigation: LoginNavigationRequest? = null
)

sealed interface LoginUserMessage {
    data class ToastRes(@param:StringRes val messageRes: Int) : LoginUserMessage
    data class ToastString(val message: String) : LoginUserMessage
    data class Lockout(val remainingMillis: Long) : LoginUserMessage
}

sealed interface LoginNavigationRequest {
    data object ToNotes : LoginNavigationRequest
}
