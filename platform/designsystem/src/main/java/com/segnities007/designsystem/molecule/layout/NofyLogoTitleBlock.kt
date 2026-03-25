package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.logo.NofyLogo
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyLogoTitleBlock(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleStyle: TextStyle? = null,
    subtitleStyle: TextStyle? = null,
    verticalSpacing: Dp = NofySpacing.logoBlockVerticalSpacing,
) {
    val resolvedTitle = titleStyle ?: NofyThemeTokens.typography.titleLarge
    val resolvedSubtitle = subtitleStyle ?: NofyThemeTokens.typography.bodyMedium
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        NofyLogo()
        NofyText(
            text = title,
            style = resolvedTitle,
            modifier = Modifier.semantics { heading() }
        )
        if (!subtitle.isNullOrBlank()) {
            NofySupportingText(
                text = subtitle,
                style = resolvedSubtitle,
            )
        }
    }
}

@NofyPreview
@Composable
private fun NofyLogoTitleBlockPreview() {
    NofyPreviewSurface {
        NofyLogoTitleBlock(
            title = "Welcome",
            subtitle = "Subtitle copy for context.",
        )
    }
}
