package com.segnities007.login.presentation.biometric

import androidx.biometric.BiometricPrompt
import com.segnities007.biometric.BiometricAuthenticator

/** [BiometricAuthenticator] と文言 [BiometricPromptContent] で [LoginBiometricHandler] を実装する。 */
internal class LoginBiometricHandlerImpl(
    private val biometricAuthenticator: BiometricAuthenticator,
    private val prompt: BiometricPromptContent
) : LoginBiometricHandler {

    override fun authenticate(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!biometricAuthenticator.isStrongBiometricAvailable()) {
            onError(prompt.failureMessage)
            return
        }

        biometricAuthenticator.authenticate(
            title = prompt.title,
            subtitle = prompt.subtitle,
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = { _, errString -> onError(errString.toString()) },
            onFailed = { onError(prompt.failureMessage) }
        )
    }
}
