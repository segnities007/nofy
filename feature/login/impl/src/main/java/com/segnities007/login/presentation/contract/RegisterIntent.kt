package com.segnities007.login.presentation.contract

/**
 * 新規登録画面から [com.segnities007.login.presentation.viewmodel.RegisterViewModel] へ渡すユーザー操作。
 */
sealed interface RegisterIntent {
    /**
     * 初回パスワード登録を試みる。呼び出し側は使用後にバイト配列をゼロクリアすること。
     */
    data class SubmitRegistration(
        val passwordBytes: ByteArray,
        val confirmPasswordBytes: ByteArray
    ) : RegisterIntent

    /** 表示済みの [RegisterUserMessage] をクリアする。 */
    data object ConsumeUserMessage : RegisterIntent

    /** 処理済みの [RegisterNavigationRequest] をクリアする。 */
    data object ConsumeNavigation : RegisterIntent
}
