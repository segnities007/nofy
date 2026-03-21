package com.segnities007.setting.presentation.contract

import com.segnities007.settings.ThemeMode

data class SettingState(
    val currentSection: SettingsSection = SettingsSection.Appearance,
    val themeMode: ThemeMode = ThemeMode.Light,
    val fontScale: Float = 1f,
    val isBiometricEnabled: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isBiometricUpdating: Boolean = false,
    val isPasswordUpdating: Boolean = false,
    val isResetting: Boolean = false
) {
    val isAnyLoading: Boolean
        get() = isBiometricUpdating || isPasswordUpdating || isResetting

    val canUpdatePassword: Boolean
        get() = currentPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            !isPasswordUpdating
}
