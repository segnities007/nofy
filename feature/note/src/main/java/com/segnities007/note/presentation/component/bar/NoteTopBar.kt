package com.segnities007.note.presentation.component.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.bar.NofyFloatingTopBar
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R

@Composable
internal fun NoteTopBar(
    title: String,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NofyFloatingTopBar(modifier = modifier){
        NofyIconButton(
            imageVector = NofyIcons.Delete,
            contentDescription = stringResource(R.string.note_cd_delete_page),
            onClick = onDelete,
            enabled = canDelete
        )

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

        NofyIconButton(
            imageVector = NofyIcons.Lock,
            contentDescription = stringResource(R.string.note_cd_lock),
            onClick = onLock
        )
    }
}

@NofyPreview
@Composable
private fun NoteTopBarPreview() {
    NofyPreviewSurface {
        NoteTopBar(
            title = "Secure note",
            canDelete = true,
            onDelete = {},
            onLock = {}
        )
    }
}
