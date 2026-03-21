package com.segnities007.login.presentation

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.segnities007.biometric.BiometricAuthenticator

@Composable
internal fun rememberBiometricHandler(
    biometricAuthenticator: BiometricAuthenticator?,
    authenticatePrompt: BiometricPromptContent,
    cryptoPrompt: BiometricPromptContent = authenticatePrompt
): LoginBiometricHandler {
    return remember(biometricAuthenticator, authenticatePrompt, cryptoPrompt) {
        biometricAuthenticator?.let {
            LoginBiometricHandlerImpl(
                biometricAuthenticator = it,
                authenticatePrompt = authenticatePrompt,
                cryptoPrompt = cryptoPrompt
            )
        } ?: UnavailableLoginBiometricHandler(
            authenticateFailureMessage = authenticatePrompt.failureMessage,
            cryptoFailureMessage = cryptoPrompt.failureMessage
        )
    }
}

private class UnavailableLoginBiometricHandler(
    private val authenticateFailureMessage: String,
    private val cryptoFailureMessage: String
) : LoginBiometricHandler {
    override fun authenticate(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        onError(authenticateFailureMessage)
    }

    override fun authenticateWithCrypto(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        onError(cryptoFailureMessage)
    }
}
