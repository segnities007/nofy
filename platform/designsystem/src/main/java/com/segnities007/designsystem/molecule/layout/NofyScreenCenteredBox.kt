package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.indicator.NofyLoadingIndicator
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface

@Composable
fun NofyScreenCenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@NofyPreview
@Composable
private fun NofyScreenCenteredBoxPreview() {
    NofyPreviewSurface {
        NofyScreenCenteredBox {
            NofyLoadingIndicator()
        }
    }
}
