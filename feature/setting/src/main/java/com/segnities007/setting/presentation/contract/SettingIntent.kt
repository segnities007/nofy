package com.segnities007.setting.presentation.contract

import com.segnities007.settings.ThemeMode

sealed interface SettingIntent {
    data class SelectSection(val section: SettingsSection) : SettingIntent
    data class SelectThemeMode(val themeMode: ThemeMode) : SettingIntent
    data class ChangeFontScale(val fontScale: Float) : SettingIntent
    data class SavePassword(
        val currentPassword: String,
        val newPassword: String,
        val confirmPassword: String
    ) : SettingIntent
    data class ResetApp(val currentPassword: String) : SettingIntent
    data object Lock : SettingIntent
}
