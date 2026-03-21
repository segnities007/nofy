package com.segnities007.login.presentation

import androidx.biometric.BiometricPrompt

interface LoginBiometricHandler {
    /**
     * 暗号化なしの生体認証を実行する
     */
    fun authenticate(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    )

    /**
     * 暗号化オブジェクト（CryptoObject）を使用した生体認証を実行する
     */
    fun authenticateWithCrypto(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    )
}
