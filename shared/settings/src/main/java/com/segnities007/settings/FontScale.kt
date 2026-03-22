package com.segnities007.settings

import kotlin.math.abs

val FontScalePresets = listOf(
    0.85f,
    1.0f,
    1.15f,
    1.3f,
    1.4f
)

fun snapToSupportedFontScale(fontScale: Float): Float {
    return FontScalePresets.minBy { preset ->
        abs(preset - fontScale)
    }
}

fun fontScalePresetIndex(fontScale: Float): Int {
    val snappedFontScale = snapToSupportedFontScale(fontScale)
    return FontScalePresets.indexOf(snappedFontScale)
}
