package com.segnities007.designsystem.atom.floatingbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyElevation
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.designsystem.theme.Shapes

/** フローティングバーの高さ・パディング・形状・アクション色の既定値。 */
object NofyFloatingBarDefaults {
    val HorizontalPadding = NofySpacing.lg
    val TopPadding = NofySpacing.lg
    val BottomPadding = NofySpacing.lg
    val TopBarHeight = NofySpacing.floatingBarTopMinHeight
    val BottomBarHeight = NofySpacing.floatingBarBottomMinHeight
    val ContainerShape = Shapes.extraLarge
    val TopBarOverlayHeight = TopPadding + TopBarHeight
    val BottomBarReservedSpace = BottomPadding + BottomBarHeight
    val ContentInset = NofySpacing.screenEdgeGutter

    @Composable
    @ReadOnlyComposable
    fun actionContainerColor(
        selected: Boolean = false
    ): Color {
        return if (selected) {
            NofyThemeTokens.colorScheme.secondaryContainer
        } else {
            NofyThemeTokens.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
        }
    }

    @Composable
    @ReadOnlyComposable
    fun actionIconColor(
        selected: Boolean = false,
        enabled: Boolean = true
    ): Color {
        return when {
            !enabled -> NofyThemeTokens.colorScheme.onSurface.copy(alpha = 0.38f)
            selected -> NofyThemeTokens.colorScheme.onSecondaryContainer
            else -> NofyThemeTokens.colorScheme.onSurfaceVariant
        }
    }
}

@Composable
fun NofyFloatingBar(
    minHeight: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = NofyFloatingBarDefaults.ContainerShape,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = NofySpacing.sm,
        vertical = NofySpacing.xs,
    ),
    tonalElevation: Dp = NofyElevation.floatingBarSurface,
    shadowElevation: Dp = NofyElevation.floatingBarSurface,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .border(
                border = BorderStroke(
                    width = NofySpacing.hairlineWidth,
                    color = NofyThemeTokens.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                shape = shape
            ),
        shape = shape,
        color = NofyThemeTokens.colorScheme.surfaceContainer.copy(alpha = 0.96f),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Preview
@Composable
private fun NofyFloatingBarPreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NofySpacing.xl),
                contentAlignment = Alignment.TopCenter
            ) {
                NofyFloatingBar(minHeight = NofyFloatingBarDefaults.TopBarHeight) {
                    NofyText(text = "Floating Bar")
                }
            }
        }
    }
}
