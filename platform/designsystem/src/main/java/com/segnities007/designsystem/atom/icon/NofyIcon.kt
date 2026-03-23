package com.segnities007.designsystem.atom.icon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@NofyPreview
@Composable
private fun NofyIconPreview() {
    NofyPreviewSurface {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NofySpacing.previewCanvasPadding),
                contentAlignment = Alignment.Center
            ) {
                NofyIcon(
                    imageVector = NofyIcons.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    }
}
