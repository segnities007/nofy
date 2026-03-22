package com.segnities007.datastore

class SettingsLocalDataSource(
    private val store: KeystorePreferencesStore
) {
    fun saveThemeMode(themeMode: String) {
        store.putString(KEY_THEME_MODE, themeMode)
    }

    fun getThemeMode(): String? = store.getString(KEY_THEME_MODE)

    fun saveFontScale(fontScale: Float) {
        store.putFloat(KEY_FONT_SCALE, fontScale)
    }

    fun getFontScale(defaultValue: Float = 1f): Float {
        return store.getFloat(KEY_FONT_SCALE, defaultValue)
    }

    fun clearSettings() {
        store.remove(KEY_THEME_MODE)
        store.remove(KEY_FONT_SCALE)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FONT_SCALE = "font_scale"
    }
}
