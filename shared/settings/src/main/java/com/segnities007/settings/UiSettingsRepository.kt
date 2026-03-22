package com.segnities007.settings

import kotlinx.coroutines.flow.StateFlow

interface UiSettingsRepository {
    val settings: StateFlow<UiSettings>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setFontScale(fontScale: Float)

    suspend fun reset()
}
