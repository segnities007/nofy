package com.segnities007.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.settings.ThemeMode
import com.segnities007.settings.UiSettings

private val DarkColorScheme = darkColorScheme(
    primary = SlateDarkPrimary,
    secondary = SlateDarkSecondary,
    tertiary = SlateDarkTertiary,
    background = SlateDarkBackground,
    surface = SlateDarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = SlateLightPrimary,
    secondary = SlateLightSecondary,
    tertiary = SlateLightTertiary,
    background = SlateLightBackground,
    surface = SlateLightSurface
)

private val GreenLightColorScheme = lightColorScheme(
    primary = GreenLightPrimary,
    secondary = GreenLightSecondary,
    tertiary = GreenLightTertiary,
    background = GreenLightBackground,
    surface = GreenLightSurface
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    secondary = GreenDarkSecondary,
    tertiary = GreenDarkTertiary,
    background = GreenDarkBackground,
    surface = GreenDarkSurface
)

private val LocalNofyUiSettings = staticCompositionLocalOf { UiSettings() }

@Composable
fun NofyTheme(
    settings: UiSettings? = null,
    content: @Composable () -> Unit
) {
    val inheritedSettings = LocalNofyUiSettings.current
    val resolvedSettings = (settings ?: inheritedSettings).sanitized()
    val density = LocalDensity.current

    val colorScheme = when (resolvedSettings.themeMode) {
        ThemeMode.Light -> LightColorScheme
        ThemeMode.Dark -> DarkColorScheme
        ThemeMode.GreenLight -> GreenLightColorScheme
        ThemeMode.GreenDark -> GreenDarkColorScheme
    }

    CompositionLocalProvider(
        LocalNofyUiSettings provides resolvedSettings,
        LocalDensity provides Density(
            density = density.density,
            fontScale = resolvedSettings.fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NofyThemePreview() {
    NofyTheme {
        NofySurface {
            NofyText(
                text = "Theme Preview",
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
