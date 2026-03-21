package com.segnities007.designsystem.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.segnities007.settings.ThemeMode
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.settings.UiSettings

@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFF5F7FA
)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFF111418,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class NofyPreview

@Composable
fun NofyPreviewSurface(
    modifier: Modifier = Modifier,
    settings: UiSettings? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val previewSettings = settings ?: UiSettings(
        themeMode = if (isSystemInDarkTheme()) ThemeMode.Dark else ThemeMode.Light
    )

    NofyTheme(settings = previewSettings) {
        NofySurface(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

@NofyPreview
@Composable
private fun NofyPreviewSurfacePreview() {
    NofyPreviewSurface {
        NofyText(text = "Preview")
    }
}
