package com.segnities007.designsystem.molecule.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBar
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyFloatingTopBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                top = NofyFloatingBarDefaults.TopPadding,
                start = NofyFloatingBarDefaults.HorizontalPadding,
                end = NofyFloatingBarDefaults.HorizontalPadding
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        NofyFloatingBar(
            minHeight = NofyFloatingBarDefaults.TopBarHeight,
            shadowElevation = NofySpacing.floatingBarTopShadowElevation,
            contentPadding = PaddingValues(
                horizontal = NofySpacing.floatingBarTopRowPaddingHorizontal,
                vertical = NofySpacing.floatingBarInnerVertical,
            ),
            content = content
        )
    }
}

@NofyPreview
@Composable
private fun NofyFloatingTopBarPreview() {
    NofyPreviewSurface {
        NofyFloatingTopBar {
            NofyText(text = "Floating Top Bar")
        }
    }
}
