package com.segnities007.setting.presentation

import com.segnities007.settings.ThemeMode

sealed interface SettingIntent {
    data class SelectSection(val section: SettingsSection) : SettingIntent
    data class SelectThemeMode(val themeMode: ThemeMode) : SettingIntent
    data class ChangeFontScale(val fontScale: Float) : SettingIntent
    data class SetBiometricEnabled(val enabled: Boolean) : SettingIntent
    data class ChangeCurrentPassword(val value: String) : SettingIntent
    data class ChangeNewPassword(val value: String) : SettingIntent
    data class ChangeConfirmPassword(val value: String) : SettingIntent
    data object SavePassword : SettingIntent
    data object ResetApp : SettingIntent
    data object Lock : SettingIntent
}
