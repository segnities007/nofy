package com.segnities007.login.presentation.biometric

import androidx.biometric.BiometricPrompt

/**
 * ログイン画面から生体認証プロンプトを起動し、結果をコールバックで返す抽象（テスト差し替え用）。
 */
fun interface LoginBiometricHandler {
    /**
     * [cryptoObject] 付きで生体プロンプトを表示する。
     *
     * @param onSuccess 認証成功時。結果の Cipher で復号・検証に進む。
     * @param onError キャンセル・エラー時。ユーザー向けメッセージを渡す。
     */
    fun authenticate(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    )
}
