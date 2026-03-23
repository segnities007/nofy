package com.segnities007.login.presentation.contract

/**
 * ログイン画面から [com.segnities007.login.presentation.viewmodel.LoginViewModel] へ渡すユーザー操作。
 */
sealed interface LoginIntent {
    /** マスターパスワードでロック解除を試みる。 */
    data class SubmitPassword(val password: String) : LoginIntent

    /** 保存済み生体シークレット経由でロック解除を開始する。 */
    data object BiometricLogin : LoginIntent

    /** 端末が STRONG 生体を提供しているか（UI のボタン表示用）。 */
    data class SetBiometricAvailability(val isAvailable: Boolean) : LoginIntent

    /** 表示済みの [LoginUserMessage] をクリアする。 */
    data object ConsumeUserMessage : LoginIntent

    /** 処理済みの [LoginNavigationRequest] をクリアする。 */
    data object ConsumeNavigation : LoginIntent
}
