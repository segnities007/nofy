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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyCardSurface(
    modifier: Modifier = Modifier,
    containerColor: Color = NofyThemeTokens.colorScheme.surfaceContainer,
    shape: Shape = NofyThemeTokens.shapes.large,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    tonalElevation: androidx.compose.ui.unit.Dp = 2.dp,
    shadowElevation: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(
                    width = 1.dp,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NofyCardSurfacePreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
