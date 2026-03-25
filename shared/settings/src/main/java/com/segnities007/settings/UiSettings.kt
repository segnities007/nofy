package com.segnities007.settings

/** サポートするフォント倍率の下限。 */
const val MinFontScale = 0.85f

/** サポートするフォント倍率の上限。 */
const val MaxFontScale = 1.40f

/** アプリ全体の見た目設定（テーマと本文スケール）。 */
data class UiSettings(
    val themeMode: ThemeMode = ThemeMode.Light,
    val fontScale: Float = 1f,
    val idleLockTimeout: IdleLockTimeoutOption = IdleLockTimeoutOption.OneMinute
) {
    fun sanitized(): UiSettings {
        return copy(fontScale = snapToSupportedFontScale(fontScale.coerceIn(MinFontScale, MaxFontScale)))
    }
}
