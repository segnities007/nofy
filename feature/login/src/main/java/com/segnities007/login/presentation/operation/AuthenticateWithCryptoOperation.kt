package com.segnities007.login.presentation.operation

import androidx.biometric.BiometricPrompt
import com.segnities007.login.presentation.LoginBiometricHandler
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class AuthenticateWithCryptoOperation(
    private val biometricHandler: LoginBiometricHandler
) {
    suspend operator fun invoke(
        cryptoObject: BiometricPrompt.CryptoObject
    ): BiometricAuthenticationResult = suspendCancellableCoroutine { continuation ->
        biometricHandler.authenticateWithCrypto(
            cryptoObject = cryptoObject,
            onSuccess = { result ->
                if (continuation.isActive) {
                    continuation.resume(BiometricAuthenticationResult.Authenticated(result))
                }
            },
            onError = { message ->
                if (continuation.isActive) {
                    continuation.resume(BiometricAuthenticationResult.Failure(message))
                }
            }
        )
    }
}

internal sealed interface BiometricAuthenticationResult {
    data class Authenticated(
        val result: BiometricPrompt.AuthenticationResult
    ) : BiometricAuthenticationResult

    data class Failure(
        val message: String
    ) : BiometricAuthenticationResult
}
