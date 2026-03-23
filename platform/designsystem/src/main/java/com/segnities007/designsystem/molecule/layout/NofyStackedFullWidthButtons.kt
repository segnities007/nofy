package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyStackedFullWidthButtons(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NofySpacing.stackedButtonGap),
        content = content,
    )
}

@NofyPreview
@Composable
private fun NofyStackedFullWidthButtonsPreview() {
    NofyPreviewSurface {
        NofyStackedFullWidthButtons {
            NofyButton(text = "First", onClick = {}, modifier = Modifier.fillMaxWidth())
            NofyButton(text = "Second", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
