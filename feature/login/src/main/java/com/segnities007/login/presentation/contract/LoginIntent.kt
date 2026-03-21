package com.segnities007.login.presentation.contract

sealed interface LoginIntent {
    data class ChangePassword(val password: String) : LoginIntent
    data object Login : LoginIntent
    data object BiometricLogin : LoginIntent
    data class SetBiometricAvailability(val isAvailable: Boolean) : LoginIntent
}
