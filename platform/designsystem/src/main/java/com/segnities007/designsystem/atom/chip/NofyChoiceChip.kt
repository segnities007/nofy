package com.segnities007.designsystem.atom.chip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NofyCardSurface(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = if (selected) {
            NofyThemeTokens.colorScheme.secondaryContainer
        } else {
            NofyThemeTokens.colorScheme.surfaceContainerLow
        },
        shape = NofyThemeTokens.shapes.small,
        contentPadding = PaddingValues(
            horizontal = NofySpacing.lg,
            vertical = NofySpacing.md,
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            NofyText(
                text = label,
                style = NofyThemeTokens.typography.titleSmall,
                color = if (selected) {
                    NofyThemeTokens.colorScheme.onSecondaryContainer
                } else {
                    NofyThemeTokens.colorScheme.onSurface
                }
            )
        }
    }
}

@NofyPreview
@Composable
private fun NofyChoiceChipPreview() {
    NofyPreviewSurface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(NofySpacing.xl),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                NofyChoiceChip(
                    label = "Selected",
                    selected = true,
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                NofyChoiceChip(
                    label = "Default",
                    selected = false,
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = NofySpacing.md)
                )
            }
        }
    }
}
