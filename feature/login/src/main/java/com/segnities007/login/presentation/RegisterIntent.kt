package com.segnities007.login.presentation

sealed interface RegisterIntent {
    data class ChangePassword(val password: String) : RegisterIntent
    data class ChangeConfirmPassword(val password: String) : RegisterIntent
    data object Register : RegisterIntent
}
