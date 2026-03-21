package com.segnities007.note.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.molecule.dialog.NofyConfirmationDialog
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.note.R
import com.segnities007.note.presentation.preview.previewNotePages
import com.segnities007.note.presentation.state.NotePageUiState

@Composable
internal fun NoteDeleteDialog(
    page: NotePageUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (page.title.isBlank()) {
        stringResource(R.string.note_delete_dialog_message)
    } else {
        stringResource(
            R.string.note_delete_dialog_message_named,
            page.title
        )
    }

    NofyConfirmationDialog(
        title = stringResource(R.string.note_delete_dialog_title),
        message = message,
        confirmLabel = stringResource(R.string.note_delete_action),
        dismissLabel = stringResource(R.string.note_cancel_action),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@NofyPreview
@Composable
private fun NoteDeleteDialogPreview() {
    NoteDeleteDialog(
        page = previewNotePages().first(),
        onConfirm = {},
        onDismiss = {}
    )
}
