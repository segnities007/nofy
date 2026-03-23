package com.segnities007.designsystem.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

private val edgeBrushTopColors = listOf(Color.Black.copy(alpha = 0.32f), Color.Transparent)
private val edgeBrushBottomColors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f))

@Composable
fun NofyBrushedFloatingBarScreen(
    body: @Composable () -> Unit,
    header: @Composable (Modifier) -> Unit,
    footer: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    showEdgeBrushes: Boolean = true,
    footerExtraModifier: Modifier = Modifier.navigationBarsPadding(),
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val topBrush = remember { Brush.verticalGradient(colors = edgeBrushTopColors) }
    val bottomBrush = remember { Brush.verticalGradient(colors = edgeBrushBottomColors) }

    Box(modifier = modifier.fillMaxSize()) {
        body()
        if (showEdgeBrushes) {
            EdgeBrushStrip(
                brush = topBrush,
                height = NofyFloatingBarDefaults.TopBarOverlayHeight,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            EdgeBrushStrip(
                brush = bottomBrush,
                height = NofySpacing.brushedBottomFadeHeight,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        header(Modifier.align(Alignment.TopCenter))
        footer(
            Modifier
                .align(Alignment.BottomCenter)
                .then(footerExtraModifier)
        )
        overlay()
    }
}

@Composable
private fun EdgeBrushStrip(
    brush: Brush,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush)
    )
}

@NofyPreview
@Composable
private fun NofyBrushedFloatingBarScreenPreview() {
    NofyPreviewSurface {
        NofyBrushedFloatingBarScreen(
            showEdgeBrushes = true,
            body = {
                Box(Modifier.fillMaxSize()) {
                    NofyText(
                        text = "Body",
                        style = NofyThemeTokens.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            },
            header = { m ->
                NofyText("Header", modifier = m, style = NofyThemeTokens.typography.titleSmall)
            },
            footer = { m ->
                NofyText("Footer", modifier = m, style = NofyThemeTokens.typography.titleSmall)
            },
        )
    }
}
