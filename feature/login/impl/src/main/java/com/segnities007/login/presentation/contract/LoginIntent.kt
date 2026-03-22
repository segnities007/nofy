package com.segnities007.login.presentation.contract

sealed interface LoginIntent {
    data class SubmitPassword(val password: String) : LoginIntent
    data object BiometricLogin : LoginIntent
    data class SetBiometricAvailability(val isAvailable: Boolean) : LoginIntent
    data object ConsumeUserMessage : LoginIntent
    data object ConsumeNavigation : LoginIntent
}
