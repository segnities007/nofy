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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
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

@Preview
@Composable
private fun NofyChoiceChipPreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
                            .padding(top = 12.dp)
                    )
                }
            }
        }
    }
}
