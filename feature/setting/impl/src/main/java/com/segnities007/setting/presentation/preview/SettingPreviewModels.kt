package com.segnities007.setting.presentation.preview

import com.segnities007.settings.ThemeMode
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingsSection

internal fun previewSettingState(
    currentSection: SettingsSection = SettingsSection.Appearance
): SettingState {
    return SettingState(
        currentSection = currentSection,
        themeMode = ThemeMode.GreenLight,
        fontScale = 1.15f,
        isBiometricEnabled = true
    )
}
