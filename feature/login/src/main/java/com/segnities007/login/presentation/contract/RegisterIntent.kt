package com.segnities007.login.presentation.contract

sealed interface RegisterIntent {
    data class SubmitRegistration(
        val password: String,
        val confirmPassword: String
    ) : RegisterIntent
}
