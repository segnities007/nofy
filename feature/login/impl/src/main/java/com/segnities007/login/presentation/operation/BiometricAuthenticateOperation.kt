package com.segnities007.login.presentation.operation

import androidx.biometric.BiometricPrompt
import com.segnities007.login.presentation.biometric.LoginBiometricHandler
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** [LoginBiometricHandler] 経由で生体プロンプトを suspend で待つ。 */
internal class BiometricAuthenticateOperation(
    private val biometricHandler: LoginBiometricHandler
) {
    suspend operator fun invoke(
        cryptoObject: BiometricPrompt.CryptoObject
    ): BiometricAuthenticationResult = suspendCancellableCoroutine { continuation ->
        biometricHandler.authenticate(
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

/**
 * ログイン画面での生体認証プロンプトの成否。
 */
internal sealed interface BiometricAuthenticationResult {
    data class Authenticated(
        val result: BiometricPrompt.AuthenticationResult
    ) : BiometricAuthenticationResult

    data class Failure(
        val message: String
    ) : BiometricAuthenticationResult
}
