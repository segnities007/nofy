package com.segnities007.login.presentation.biometric

import androidx.biometric.BiometricPrompt
import com.segnities007.biometric.BiometricAuthenticator

internal fun createLoginBiometricHandler(
    biometricAuthenticator: BiometricAuthenticator?,
    prompt: BiometricPromptContent
): LoginBiometricHandler {
    return biometricAuthenticator
        ?.takeIf(BiometricAuthenticator::isStrongBiometricAvailable)
        ?.let { LoginBiometricHandlerImpl(biometricAuthenticator = it, prompt = prompt) }
        ?: UnavailableLoginBiometricHandler(failureMessage = prompt.failureMessage)
}

internal fun LoginBiometricHandler.isBiometricAvailable(): Boolean {
    return this !is UnavailableLoginBiometricHandler
}

private class UnavailableLoginBiometricHandler(
    private val failureMessage: String
) : LoginBiometricHandler {
    override fun authenticate(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        onError(failureMessage)
    }
}
