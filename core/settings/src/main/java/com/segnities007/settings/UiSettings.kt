package com.segnities007.settings

const val MinFontScale = 0.85f
const val MaxFontScale = 1.40f

data class UiSettings(
    val themeMode: ThemeMode = ThemeMode.Light,
    val fontScale: Float = 1f
) {
    fun sanitized(): UiSettings {
        return copy(fontScale = fontScale.coerceIn(MinFontScale, MaxFontScale))
    }
}
