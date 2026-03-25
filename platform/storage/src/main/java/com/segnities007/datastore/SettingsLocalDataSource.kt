package com.segnities007.datastore

/** UI 設定（テーマ・フォントスケール・無操作ロック時間）のキー値アクセス。 */
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

    fun saveIdleLockTimeoutSeconds(seconds: String) {
        store.putString(KEY_IDLE_LOCK_TIMEOUT, seconds)
    }

    fun getIdleLockTimeoutSeconds(): String? = store.getString(KEY_IDLE_LOCK_TIMEOUT)

    fun clearSettings() {
        store.remove(KEY_THEME_MODE)
        store.remove(KEY_FONT_SCALE)
        store.remove(KEY_IDLE_LOCK_TIMEOUT)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FONT_SCALE = "font_scale"
        const val KEY_IDLE_LOCK_TIMEOUT = "idle_lock_timeout_seconds"
    }
}
