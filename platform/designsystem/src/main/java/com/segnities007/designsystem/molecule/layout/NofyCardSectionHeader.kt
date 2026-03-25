package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.atom.text.NofyTitleLargeText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyCardSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    titleColor: Color = Color.Unspecified,
    supportingColor: Color = NofyThemeTokens.colorScheme.onSurfaceVariant,
) {
    Column(modifier = modifier) {
        NofyTitleLargeText(text = title, color = titleColor)
        if (supporting != null) {
            Spacer(modifier = Modifier.height(NofySpacing.sm))
            NofySupportingText(text = supporting, color = supportingColor)
        }
    }
}

@NofyPreview
@Composable
private fun NofyCardSectionHeaderPreview() {
    NofyPreviewSurface {
        NofyCardSectionHeader(
            title = "Section",
            supporting = "Optional description for this block.",
        )
    }
}
