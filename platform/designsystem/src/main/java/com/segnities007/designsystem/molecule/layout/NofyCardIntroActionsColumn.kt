package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyCardIntroActionsColumn(
    title: String,
    supporting: String? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.Unspecified,
    supportingColor: Color = NofyThemeTokens.colorScheme.onSurfaceVariant,
    actionsTopSpacing: Dp = NofySpacing.md,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        NofyCardSectionHeader(
            title = title,
            supporting = supporting,
            titleColor = titleColor,
            supportingColor = supportingColor,
        )
        Spacer(modifier = Modifier.height(actionsTopSpacing))
        actions()
    }
}

@NofyPreview
@Composable
private fun NofyCardIntroActionsColumnPreview() {
    NofyPreviewSurface {
        NofyCardIntroActionsColumn(
            title = "Licenses",
            supporting = "Open third-party notices.",
        ) {
            NofyButton(text = "Open", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
