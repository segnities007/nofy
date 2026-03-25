package com.segnities007.designsystem.atom.divider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier)
}

@NofyPreview
@Composable
private fun NofyDividerPreview() {
    NofyPreviewSurface {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NofySpacing.previewCanvasPadding),
                contentAlignment = Alignment.Center
            ) {
                NofyDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
