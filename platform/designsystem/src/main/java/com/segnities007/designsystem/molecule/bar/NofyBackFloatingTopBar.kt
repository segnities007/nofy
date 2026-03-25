package com.segnities007.designsystem.molecule.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyBackFloatingTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    backContentDescription: String,
    modifier: Modifier = Modifier,
) {
    NofyFloatingTopBar(modifier = modifier) {
        NofyIconButton(
            imageVector = NofyIcons.Back,
            contentDescription = backContentDescription,
            onClick = onNavigateBack
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            NofyText(
                text = title,
                style = NofyThemeTokens.typography.titleLarge,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.width(NofySpacing.minTouchTarget))
    }
}

@NofyPreview
@Composable
private fun NofyBackFloatingTopBarPreview() {
    NofyPreviewSurface {
        NofyBackFloatingTopBar(
            title = "Screen title",
            onNavigateBack = {},
            backContentDescription = "Go back",
        )
    }
}
