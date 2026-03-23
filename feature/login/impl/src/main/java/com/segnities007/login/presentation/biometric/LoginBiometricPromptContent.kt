package com.segnities007.login.presentation.biometric

/** [LoginBiometricHandlerImpl] が BiometricPrompt に渡す文言セット。 */
data class BiometricPromptContent(
    val title: String,
    val subtitle: String,
    val failureMessage: String
)
