package com.segnities007.designsystem.atom.surface

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofySurface(
    modifier: Modifier = Modifier,
    color: Color = NofyThemeTokens.colorScheme.background,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        content = content
    )
}

@NofyPreview
@Composable
private fun NofySurfacePreview() {
    NofyPreviewSurface {
        NofySurface(modifier = Modifier.fillMaxSize()) {
            NofyText(
                text = "Surface",
                modifier = Modifier.padding(NofySpacing.previewCanvasPadding)
            )
        }
    }
}
