package com.segnities007.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

/** [FragmentActivity] 上で [BiometricPrompt] を表示し、STRONG 生体の可否を判定する。 */
class BiometricAuthenticator(
    private val activity: FragmentActivity
) {
    /** Class 3（STRONG）相当の生体認証が利用可能か。 */
    fun isStrongBiometricAvailable(): Boolean {
        return canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    }

    /**
     * 生体プロンプトを表示し、[cryptoObject] を認証成功時に返す。
     *
     * @param onSuccess 認証成功（[BiometricPrompt.AuthenticationResult] に Cipher 等が含まれる場合がある）
     * @param onError システムエラー・ユーザー取消（BiometricPrompt の error code とメッセージ）
     * @param onFailed 生体不一致など、再試行可能な失敗
     */
    fun authenticate(
        title: String,
        subtitle: String,
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (Int, CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun canAuthenticate(authenticators: Int): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
