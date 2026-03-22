package com.segnities007.login.presentation.contract

data class LoginState(
    val isLoading: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false
)
