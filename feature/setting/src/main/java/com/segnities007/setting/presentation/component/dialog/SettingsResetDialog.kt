package com.segnities007.setting.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.molecule.dialog.NofyDestructiveConfirmDialog
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.setting.R

@Composable
internal fun SettingsResetDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmationValue by rememberSaveable { mutableStateOf("") }

    NofyDestructiveConfirmDialog(
        title = stringResource(R.string.settings_app_reset_dialog_title),
        body = stringResource(R.string.settings_app_reset_dialog_body),
        warnings = listOf(
            stringResource(R.string.settings_app_reset_dialog_warning_notes),
            stringResource(R.string.settings_app_reset_dialog_warning_auth),
            stringResource(R.string.settings_app_reset_dialog_warning_register)
        ),
        confirmationLabel = stringResource(R.string.settings_app_reset_dialog_confirmation_label),
        confirmationHint = stringResource(
            R.string.settings_app_reset_dialog_confirmation_hint,
            stringResource(R.string.settings_app_reset_dialog_confirmation_phrase)
        ),
        confirmationValue = confirmationValue,
        expectedConfirmation = stringResource(R.string.settings_app_reset_dialog_confirmation_phrase),
        confirmLabel = stringResource(R.string.settings_app_reset_dialog_confirm),
        dismissLabel = stringResource(R.string.settings_app_reset_dialog_dismiss),
        onConfirmationValueChange = { confirmationValue = it },
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@NofyPreview
@Composable
private fun SettingsResetDialogPreview() {
    NofyPreviewSurface {
        SettingsResetDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
