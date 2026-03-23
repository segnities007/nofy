package com.segnities007.datastore

import com.segnities007.settings.MaxFontScale
import com.segnities007.settings.MinFontScale
import com.segnities007.settings.ThemeMode
import com.segnities007.settings.UiSettings
import com.segnities007.settings.UiSettingsRepository
import com.segnities007.settings.snapToSupportedFontScale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** [SettingsLocalDataSource] 経由でテーマとフォント倍率を永続化し、[StateFlow] で公開する。 */
class SecureUiSettingsRepository(
    private val settingsLocalDataSource: SettingsLocalDataSource
) : UiSettingsRepository {

    private val _settings = MutableStateFlow(loadSettings())

    override val settings: StateFlow<UiSettings> = _settings.asStateFlow()

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsLocalDataSource.saveThemeMode(themeMode.storageValue)
        _settings.update { it.copy(themeMode = themeMode) }
    }

    override suspend fun setFontScale(fontScale: Float) {
        val sanitizedScale = snapToSupportedFontScale(
            fontScale.coerceIn(MinFontScale, MaxFontScale)
        )
        settingsLocalDataSource.saveFontScale(sanitizedScale)
        _settings.update { it.copy(fontScale = sanitizedScale) }
    }

    override suspend fun reset() {
        settingsLocalDataSource.clearSettings()
        _settings.value = loadSettings()
    }

    private fun loadSettings(): UiSettings {
        return UiSettings(
            themeMode = ThemeMode.fromStorage(settingsLocalDataSource.getThemeMode()),
            fontScale = settingsLocalDataSource.getFontScale()
        ).sanitized()
    }
}
