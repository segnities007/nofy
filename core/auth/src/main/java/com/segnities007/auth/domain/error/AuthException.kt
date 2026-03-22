package com.segnities007.auth.domain.error

sealed class AuthException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    data object AlreadyRegistered : AuthException("Already registered")
    data object NotRegistered : AuthException("Not registered")
    data object InvalidPassword : AuthException("Invalid password")
    data object BiometricNotEnrolled : AuthException("Biometric not enrolled")
    data object UntrustedEnvironment : AuthException("Untrusted environment")
    data class LockedOut(val remainingMillis: Long) : AuthException("Locked out")
    data class PasswordTooShort(val minimumLength: Int) : AuthException("Password too short")
    data object PasswordTooCommon : AuthException("Password too common")
}
