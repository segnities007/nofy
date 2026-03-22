package com.segnities007.designsystem.atom.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.icon.NofyIcon
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    val iconTint = NofyFloatingBarDefaults.actionIconColor(
        selected = selected,
        enabled = enabled
    )

    NofyIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        selected = selected
    ) {
        NofyIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint
        )
    }
}

@Composable
fun NofyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    content: @Composable () -> Unit
) {
    val containerColor = NofyFloatingBarDefaults.actionContainerColor(selected = selected)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(NofyThemeTokens.shapes.large)
                .background(containerColor)
        ) {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                content = content
            )
        }
    }
}

@Preview
@Composable
private fun NofyIconButtonPreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NofyIconButton(
                    imageVector = NofyIcons.Settings,
                    contentDescription = "Settings",
                    onClick = {}
                )
            }
        }
    }
}
