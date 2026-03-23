package com.segnities007.settings

import kotlin.math.abs

/** 設定 UI と永続化で許可する離散フォント倍率。 */
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

/** [snapToSupportedFontScale] 後の値が [FontScalePresets] の何番目か。 */
fun fontScalePresetIndex(fontScale: Float): Int {
    val snappedFontScale = snapToSupportedFontScale(fontScale)
    return FontScalePresets.indexOf(snappedFontScale)
}
