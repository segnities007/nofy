package com.segnities007.login.presentation.contract

import androidx.annotation.StringRes

data class RegisterState(
    val isLoading: Boolean = false,
    val pendingUserMessage: RegisterUserMessage? = null,
    val pendingNavigation: RegisterNavigationRequest? = null
)

sealed interface RegisterUserMessage {
    data class ToastRes(@param:StringRes val messageRes: Int) : RegisterUserMessage
    data class ToastResArgs(
        @param:StringRes val messageRes: Int,
        val formatArgs: List<Any>
    ) : RegisterUserMessage
}

/**
 * 遷移前に [preludeToastRes] があれば Toast を一度表示してからログインへ戻る。
 */
sealed interface RegisterNavigationRequest {
    data class ToLogin(@param:StringRes val preludeToastRes: Int? = null) : RegisterNavigationRequest
}
