package com.segnities007.setting.presentation.contract

import androidx.annotation.StringRes
import com.segnities007.settings.IdleLockTimeoutOption
import com.segnities007.settings.ThemeMode

/**
 * 設定画面で一度だけ消費するユーザー向けフィードバック（Toast 等）。
 */
sealed interface SettingUserMessage {
    /** 文字列リソース ID で Toast を一度表示する。 */
    data class ToastRes(@param:StringRes val messageRes: Int) : SettingUserMessage
}

/**
 * 設定から認証フローへ遷移するなど、画面外への一方向ナビ要求。
 */
sealed interface SettingNavigationRequest {
    /** ロック後など、ログイン画面へ遷移する。 */
    data object ToLogin : SettingNavigationRequest

    /** 新規登録（サインアップ）フローへ遷移する。 */
    data object ToSignUp : SettingNavigationRequest
}

/** 設定画面が購読する単一の表示・操作状態。 */
data class SettingState(
    /** 現在選択中の設定タブ。 */
    val currentSection: SettingsSection = SettingsSection.Appearance,

    /** 永続化されたテーマ（見た目セクション表示用）。 */
    val themeMode: ThemeMode = ThemeMode.Light,

    /** 本文フォントのスケール（1.0 が既定）。 */
    val fontScale: Float = 1f,

    /** フォアグラウンド無操作でロックするまでの時間。 */
    val idleLockTimeout: IdleLockTimeoutOption = IdleLockTimeoutOption.OneMinute,

    /** 生体ログインが有効か（リポジトリ由来）。 */
    val isBiometricEnabled: Boolean = false,
    /**
     * パスワード変更成功のたびにインクリメントする。UI は composition 上の下書きをクリアするために購読する。
     * 平文下書き自体は [PasswordChangeDraft] として UI 層に置く（同型の KDoc 参照）。
     */
    val passwordDraftClearNonce: Int = 0,

    /** マスターパスワード変更 API 実行中。 */
    val isPasswordUpdating: Boolean = false,

    /** アプリ初期化（リセット）実行中。 */
    val isResetting: Boolean = false,

    /** 生体シークレット削除・無効化の実行中。 */
    val isDisablingBiometric: Boolean = false,

    /** 生体登録用モーダル／ダイアログを表示するか。 */
    val biometricEnrollmentDialogVisible: Boolean = false,

    /** 生体登録（暗号化保存）の非同期処理が走っているか。 */
    val isBiometricEnrollmentInProgress: Boolean = false,

    /** 未消費のユーザー向けメッセージ（Toast 等）。表示後は Consume で null にする。 */
    val pendingUserMessage: SettingUserMessage? = null,

    /** 未処理の画面外ナビ要求。処理後は Consume で null にする。 */
    val pendingNavigation: SettingNavigationRequest? = null
) {
    /** いずれかの長時間操作が進行中か。 */
    val isAnyLoading: Boolean
        get() = isPasswordUpdating || isResetting || isDisablingBiometric

    /** 生体トグル操作不可（無効化処理中または登録フロー中） */
    val isBiometricSwitchBusy: Boolean
        get() = isDisablingBiometric || isBiometricEnrollmentInProgress
}
