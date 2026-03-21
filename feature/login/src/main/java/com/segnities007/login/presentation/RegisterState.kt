package com.segnities007.login.presentation

data class RegisterState(
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false
)
