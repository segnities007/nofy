package com.segnities007.login.presentation.biometric

import androidx.biometric.BiometricPrompt

fun interface LoginBiometricHandler {
    fun authenticate(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    )
}
