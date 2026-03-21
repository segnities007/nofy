package com.segnities007.auth.domain.error

sealed class AuthException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    data object AlreadyRegistered : AuthException("Already registered")
    data object NotRegistered : AuthException("Not registered")
    data object InvalidPassword : AuthException("Invalid password")
}
