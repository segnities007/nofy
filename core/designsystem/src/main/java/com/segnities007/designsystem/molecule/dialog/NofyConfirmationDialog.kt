package com.segnities007.designsystem.molecule.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.segnities007.designsystem.atom.button.NofyTextButton
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    rejectObscuredConfirmTouch: Boolean = false,
    rejectObscuredDismissTouch: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            NofyText(text = title)
        },
        text = {
            NofyText(text = message)
        },
        confirmButton = {
            NofyTextButton(
                text = confirmLabel,
                onClick = onConfirm,
                rejectObscuredTouches = rejectObscuredConfirmTouch
            )
        },
        dismissButton = {
            NofyTextButton(
                text = dismissLabel,
                onClick = onDismiss,
                rejectObscuredTouches = rejectObscuredDismissTouch
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun NofyConfirmationDialogPreview() {
    NofyTheme {
        NofyConfirmationDialog(
            title = "Delete page?",
            message = "This action cannot be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
