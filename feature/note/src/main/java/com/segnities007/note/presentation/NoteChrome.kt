package com.segnities007.note.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcon
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.bar.NofyFloatingBottomBar
import com.segnities007.designsystem.molecule.bar.NofyFloatingTopBar
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R

internal data class NoteChromeState(
    val title: String,
    val canDelete: Boolean,
    val currentPage: Int,
    val totalPages: Int,
    val canNavigatePrevious: Boolean,
    val canNavigateNext: Boolean,
    val canShowBars: Boolean
)

internal fun NoteState.toChromeState(untitledTitle: String): NoteChromeState {
    val currentPage = currentPage
    return NoteChromeState(
        title = currentPage?.title?.ifBlank { untitledTitle } ?: untitledTitle,
        canDelete = currentPage?.let { it.noteId != null || it.content.isNotBlank() } == true,
        currentPage = currentPageIndex,
        totalPages = pages.size,
        canNavigatePrevious = currentPageIndex > 0,
        canNavigateNext = currentPageIndex < pages.lastIndex,
        canShowBars = !isLoading && (error == null || pages.any { !it.isBlank })
    )
}

@Composable
internal fun NoteTopBar(
    title: String,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onLock: () -> Unit
) {
    NofyFloatingTopBar {
        NofyIconButton(
            onClick = onDelete,
            enabled = canDelete
        ) {
            NofyIcon(
                imageVector = NofyIcons.Delete,
                contentDescription = stringResource(R.string.note_cd_delete_page)
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            NofyText(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = NofyThemeTokens.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        NofyIconButton(onClick = onLock) {
            NofyIcon(
                imageVector = NofyIcons.Lock,
                contentDescription = stringResource(R.string.note_cd_lock)
            )
        }
    }
}

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
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(NofyThemeTokens.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
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

@Composable
private fun RowScope.BottomBarIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true
) {
    val containerColor = if (selected) {
        NofyThemeTokens.colorScheme.secondaryContainer
    } else {
        NofyThemeTokens.colorScheme.surfaceContainerHigh.copy(alpha = 0.64f)
    }
    val contentColor = when {
        !enabled -> NofyThemeTokens.colorScheme.onSurface.copy(alpha = 0.38f)
        selected -> NofyThemeTokens.colorScheme.onSecondaryContainer
        else -> NofyThemeTokens.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(containerColor)
        ) {
            NofyIconButton(
                onClick = onClick,
                enabled = enabled
            ) {
                NofyIcon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor
                )
            }
        }
    }
}
