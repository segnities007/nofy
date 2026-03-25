package com.segnities007.note.presentation.component.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.bar.NofyFloatingBottomBar
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R

@Composable
internal fun NoteBottomBar(
    isPreviewEnabled: Boolean,
    currentPage: Int,
    totalPages: Int,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTogglePreview: () -> Unit,
    onSettings: () -> Unit
) {
    NofyFloatingBottomBar {
        BottomBarIconAction(
            icon = NofyIcons.Back,
            contentDescription = stringResource(R.string.note_cd_previous_page),
            enabled = canNavigatePrevious,
            onClick = onPrevious
        )
        BottomBarIconAction(
            icon = NofyIcons.Preview,
            contentDescription = stringResource(R.string.note_cd_toggle_preview),
            selected = isPreviewEnabled,
            onClick = onTogglePreview
        )
        Box(
            modifier = Modifier.weight(1.5f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(NofyThemeTokens.shapes.small)
                    .background(NofyThemeTokens.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f))
                    .padding(
                        horizontal = NofySpacing.xl,
                        vertical = NofySpacing.md,
                    )
            ) {
                NofyText(
                    text = stringResource(R.string.note_page_counter, currentPage + 1, totalPages),
                    style = NofyThemeTokens.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        BottomBarIconAction(
            icon = NofyIcons.Settings,
            contentDescription = stringResource(R.string.note_cd_open_settings),
            onClick = onSettings
        )
        BottomBarIconAction(
            icon = NofyIcons.Forward,
            contentDescription = stringResource(R.string.note_cd_next_page),
            enabled = canNavigateNext,
            onClick = onNext
        )
    }
}

@NofyPreview
@Composable
private fun NoteBottomBarPreview() {
    NofyPreviewSurface {
        NoteBottomBar(
            isPreviewEnabled = false,
            currentPage = 0,
            totalPages = 2,
            canNavigatePrevious = false,
            canNavigateNext = true,
            onPrevious = {},
            onNext = {},
            onTogglePreview = {},
            onSettings = {}
        )
    }
}

@Composable
private fun RowScope.BottomBarIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        NofyIconButton(
            imageVector = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            selected = selected,
            enabled = enabled
        )
    }
}
