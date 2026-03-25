package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyWeightedFormShell(
    hero: @Composable () -> Unit,
    form: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: Dp = NofySpacing.screenEdgeGutter,
    sectionSpacing: Dp = NofySpacing.formFieldGap,
    topSpacerWeight: Float = 1f,
    middleSpacerWeight: Float = 1f,
    bottomSpacerWeight: Float = 0.5f,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        ) {
            Spacer(Modifier.weight(topSpacerWeight))
            hero()
            Spacer(Modifier.weight(middleSpacerWeight))
            form()
            Spacer(Modifier.weight(bottomSpacerWeight))
        }
    }
}

@NofyPreview
@Composable
private fun NofyWeightedFormShellPreview() {
    NofyPreviewSurface {
        NofyWeightedFormShell(
            hero = {
                NofyText(
                    text = "Hero",
                    style = NofyThemeTokens.typography.titleLarge,
                )
            },
            form = {
                NofyText(
                    text = "Form area",
                    style = NofyThemeTokens.typography.bodyLarge,
                )
            },
        )
    }
}
