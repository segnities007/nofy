package com.segnities007.login.presentation.biometric

import androidx.biometric.BiometricPrompt
import com.segnities007.biometric.BiometricAuthenticator

internal fun createLoginBiometricHandler(
    biometricAuthenticator: BiometricAuthenticator?,
    authenticatePrompt: BiometricPromptContent,
    cryptoPrompt: BiometricPromptContent = authenticatePrompt
): LoginBiometricHandler {
    return biometricAuthenticator
        ?.takeIf(BiometricAuthenticator::isStrongBiometricAvailable)
        ?.let {
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

internal fun LoginBiometricHandler.isBiometricAvailable(): Boolean {
    return this !is UnavailableLoginBiometricHandler
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
