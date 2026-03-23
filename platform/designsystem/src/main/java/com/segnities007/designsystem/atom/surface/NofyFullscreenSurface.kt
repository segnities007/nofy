package com.segnities007.designsystem.atom.surface

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface

@Composable
fun NofyFullscreenSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    NofySurface(modifier = modifier.fillMaxSize(), content = content)
}

@NofyPreview
@Composable
private fun NofyFullscreenSurfacePreview() {
    NofyPreviewSurface {
        NofyFullscreenSurface {
            NofyText(text = "Fullscreen surface")
        }
    }
}
