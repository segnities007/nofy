package com.segnities007.designsystem.molecule.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun NofyFloatingBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = NofyFloatingBarDefaults.HorizontalPadding,
                end = NofyFloatingBarDefaults.HorizontalPadding,
                bottom = NofyFloatingBarDefaults.BottomPadding
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        NofyFloatingBar(
            minHeight = NofyFloatingBarDefaults.BottomBarHeight,
            contentPadding = PaddingValues(
                horizontal = NofySpacing.floatingBarBottomRowPaddingHorizontal,
                vertical = NofySpacing.floatingBarInnerVertical,
            ),
            content = content
        )
    }
}

@NofyPreview
@Composable
private fun NofyFloatingBottomBarPreview() {
    NofyPreviewSurface {
        NofyFloatingBottomBar {
            NofyText(text = "Floating Bottom Bar")
        }
    }
}
