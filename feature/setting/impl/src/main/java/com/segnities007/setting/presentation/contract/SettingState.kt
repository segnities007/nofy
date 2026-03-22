package com.segnities007.setting.presentation.contract

import androidx.annotation.StringRes
import com.segnities007.settings.ThemeMode

sealed interface SettingUserMessage {
    data class ToastRes(@param:StringRes val messageRes: Int) : SettingUserMessage
}

sealed interface SettingNavigationRequest {
    data object ToLogin : SettingNavigationRequest
    data object ToSignUp : SettingNavigationRequest
}

data class SettingState(
    val currentSection: SettingsSection = SettingsSection.Appearance,
    val themeMode: ThemeMode = ThemeMode.Light,
    val fontScale: Float = 1f,
    val isBiometricEnabled: Boolean = false,
    /**
     * パスワード変更成功のたびにインクリメントする。UI は composition 上の下書きをクリアするために購読する。
     * 平文下書き自体は [PasswordChangeDraft] として UI 層に置く（同型の KDoc 参照）。
     */
    val passwordDraftClearNonce: Int = 0,
    val isPasswordUpdating: Boolean = false,
    val isResetting: Boolean = false,
    val isDisablingBiometric: Boolean = false,
    val biometricEnrollmentDialogVisible: Boolean = false,
    val isBiometricEnrollmentInProgress: Boolean = false,
    val pendingUserMessage: SettingUserMessage? = null,
    val pendingNavigation: SettingNavigationRequest? = null
) {
    val isAnyLoading: Boolean
        get() = isPasswordUpdating || isResetting || isDisablingBiometric

    /** 生体トグル操作不可（無効化処理中または登録フロー中） */
    val isBiometricSwitchBusy: Boolean
        get() = isDisablingBiometric || isBiometricEnrollmentInProgress
}
