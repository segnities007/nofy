package com.segnities007.login.presentation

import androidx.biometric.BiometricPrompt
import com.segnities007.biometric.BiometricAuthenticator

data class BiometricPromptContent(
    val title: String,
    val subtitle: String,
    val failureMessage: String
)

class LoginBiometricHandlerImpl(
    private val biometricAuthenticator: BiometricAuthenticator,
    private val authenticatePrompt: BiometricPromptContent,
    private val cryptoPrompt: BiometricPromptContent = authenticatePrompt
) : LoginBiometricHandler {

    override fun authenticate(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!biometricAuthenticator.isBiometricAvailable()) {
            onError(authenticatePrompt.failureMessage)
            return
        }

        biometricAuthenticator.authenticate(
            title = authenticatePrompt.title,
            subtitle = authenticatePrompt.subtitle,
            onSuccess = onSuccess,
            onError = { _, errString -> onError(errString.toString()) },
            onFailed = { onError(authenticatePrompt.failureMessage) }
        )
    }

    override fun authenticateWithCrypto(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!biometricAuthenticator.isBiometricAvailable()) {
            onError(cryptoPrompt.failureMessage)
            return
        }

        biometricAuthenticator.authenticate(
            title = cryptoPrompt.title,
            subtitle = cryptoPrompt.subtitle,
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = { _, errString -> onError(errString.toString()) },
            onFailed = { onError(cryptoPrompt.failureMessage) }
        )
    }
}
