package com.segnities007.settings

enum class ThemeMode(val storageValue: String) {
    Light("light"),
    Dark("dark"),
    GreenLight("green_light"),
    GreenDark("green_dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: Light
        }
    }
}
