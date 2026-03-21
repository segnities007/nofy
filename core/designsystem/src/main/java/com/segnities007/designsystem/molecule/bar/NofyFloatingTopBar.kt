package com.segnities007.designsystem.molecule.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBar
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyFloatingTopBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = NofyFloatingBarDefaults.HorizontalPadding,
                top = NofyFloatingBarDefaults.TopPadding,
                end = NofyFloatingBarDefaults.HorizontalPadding
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        NofyFloatingBar(
            minHeight = NofyFloatingBarDefaults.TopBarHeight,
            shadowElevation = 6.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            content = content
        )
    }
}

@Preview
@Composable
private fun NofyFloatingTopBarPreview() {
    NofyTheme {
        NofyFloatingTopBar {
            NofyText(text = "Floating Top Bar")
        }
    }
}
