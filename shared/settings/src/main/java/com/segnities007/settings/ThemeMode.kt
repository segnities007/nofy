package com.segnities007.settings

/** 永続化用文字列 [storageValue] と対応するテーマバリアント。 */
enum class ThemeMode(val storageValue: String) {
    Light("light"),
    Dark("dark"),
    GreenLight("green_light"),
    GreenDark("green_dark");

    companion object {
        /** 不明な値は [Light] にフォールバックする。 */
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: Light
        }
    }
}
