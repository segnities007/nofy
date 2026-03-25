package com.segnities007.designsystem.atom.surface

import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyElevation
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyCardSurface(
    modifier: Modifier = Modifier,
    containerColor: Color = NofyThemeTokens.colorScheme.surfaceContainer,
    shape: Shape = NofyThemeTokens.shapes.large,
    contentPadding: PaddingValues = PaddingValues(NofySpacing.screenEdgeGutter),
    tonalElevation: androidx.compose.ui.unit.Dp = NofyElevation.cardTonal,
    shadowElevation: androidx.compose.ui.unit.Dp = NofyElevation.none,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(
                    width = NofySpacing.hairlineWidth,
                    color = NofyThemeTokens.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                shape = shape
            ),
        color = containerColor,
        shape = shape,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(NofySpacing.lg),
            content = content
        )
    }
}

@NofyPreview
@Composable
private fun NofyCardSurfacePreview() {
    NofyPreviewSurface {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NofySpacing.previewCanvasPadding),
                contentAlignment = Alignment.Center
            ) {
                NofyCardSurface {
                    NofyText(text = "Card title", style = NofyThemeTokens.typography.titleMedium)
                    NofyText(text = "Card body")
                }
            }
        }
    }
}
