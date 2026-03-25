package com.segnities007.login.presentation.contract

import androidx.annotation.StringRes

/** 新規登録画面が購読する単一の表示状態。 */
data class RegisterState(
    /** 登録・生体保存などの非同期処理中。 */
    val isLoading: Boolean = false,

    /** 未消費のユーザー向けメッセージ。 */
    val pendingUserMessage: RegisterUserMessage? = null,

    /** 未処理の遷移要求（ログインへ戻る等）。 */
    val pendingNavigation: RegisterNavigationRequest? = null
)

/**
 * 新規登録画面で一度だけ消費するユーザー向けフィードバック。
 */
sealed interface RegisterUserMessage {
    /** 文字列リソースで Toast を一度表示する。 */
    data class ToastRes(@param:StringRes val messageRes: Int) : RegisterUserMessage

    /** フォーマット引数付きの文字列リソースで Toast を一度表示する。 */
    data class ToastResArgs(
        @param:StringRes val messageRes: Int,
        val formatArgs: List<Any>
    ) : RegisterUserMessage
}

/**
 * 遷移前に [preludeToastRes] があれば Toast を一度表示してからログインへ戻る。
 */
sealed interface RegisterNavigationRequest {
    /**
     * ログイン画面へ戻る。非 null なら遷移前にそのリソースで Toast を出す。
     */
    data class ToLogin(@param:StringRes val preludeToastRes: Int? = null) : RegisterNavigationRequest
}
