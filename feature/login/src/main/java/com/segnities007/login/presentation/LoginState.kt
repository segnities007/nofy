package com.segnities007.login.presentation

data class LoginState(
    val password: String = "",
    val isLoading: Boolean = false,
    val isBiometricAvailable: Boolean = false
)
