package com.segnities007.login.presentation.contract

sealed interface RegisterIntent {
    data class SubmitRegistration(
        val passwordBytes: ByteArray,
        val confirmPasswordBytes: ByteArray
    ) : RegisterIntent
}
