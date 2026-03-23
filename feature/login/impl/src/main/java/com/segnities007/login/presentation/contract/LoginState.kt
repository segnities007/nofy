package com.segnities007.login.presentation.contract

import androidx.annotation.StringRes

/** ログイン画面が購読する単一の表示状態。 */
data class LoginState(
    /** パスワード／生体によるロック解除試行中。 */
    val isLoading: Boolean = false,

    /** 端末が STRONG 生体を提供しているか（ボタン表示用）。 */
    val isBiometricAvailable: Boolean = false,

    /** 生体ログインが設定済みか。 */
    val isBiometricEnabled: Boolean = false,

    /** 未消費のユーザー向けメッセージ。 */
    val pendingUserMessage: LoginUserMessage? = null,

    /** 未処理の遷移要求（成功時にメインへ等）。 */
    val pendingNavigation: LoginNavigationRequest? = null
)

/**
 * ログイン画面で一度だけ消費するユーザー向けフィードバック。
 */
sealed interface LoginUserMessage {
    /** 文字列リソースで Toast を一度表示する。 */
    data class ToastRes(@param:StringRes val messageRes: Int) : LoginUserMessage

    /** 生の文字列で Toast を一度表示する。 */
    data class ToastString(val message: String) : LoginUserMessage

    /** 試行回数制限によるロックアウト。残り時間を UI に示す。 */
    data class Lockout(val remainingMillis: Long) : LoginUserMessage
}

/**
 * ログイン完了後のメイン遷移など、画面外への一方向ナビ要求。
 */
sealed interface LoginNavigationRequest {
    /** 認証成功後、ノート（メイン）画面へ遷移する。 */
    data object ToNotes : LoginNavigationRequest
}
