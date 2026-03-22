package com.segnities007.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleTest {

    @Test
    fun snapToSupportedFontScale_returnsNearestPreset() {
        assertEquals(1.15f, snapToSupportedFontScale(1.1f))
        assertEquals(0.85f, snapToSupportedFontScale(0.8f))
        assertEquals(1.4f, snapToSupportedFontScale(1.38f))
    }

    @Test
    fun sanitized_snapsArbitraryScaleToPreset() {
        val sanitized = UiSettings(fontScale = 1.1f).sanitized()

        assertEquals(1.15f, sanitized.fontScale)
    }
}
