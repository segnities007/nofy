package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.atom.toggle.NofySwitch
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyLabelTrailingRow(
    label: String,
    modifier: Modifier = Modifier,
    labelStyle: TextStyle? = null,
    trailingContent: @Composable RowScope.() -> Unit,
) {
    val style = labelStyle ?: NofyThemeTokens.typography.titleMedium
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NofyText(
            text = label,
            modifier = Modifier.weight(1f),
            style = style,
        )
        trailingContent()
    }
}

@NofyPreview
@Composable
private fun NofyLabelTrailingRowPreview() {
    NofyPreviewSurface {
        NofyLabelTrailingRow(
            label = "Enable feature",
            trailingContent = {
                NofySwitch(checked = true, onCheckedChange = {})
            },
        )
    }
}
