package com.segnities007.designsystem.molecule.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyTextButton
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyFormDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            NofyText(text = title)
        },
        text = {
            DialogFormBody(
                message = message,
                content = content
            )
        },
        confirmButton = {
            NofyTextButton(
                text = confirmLabel,
                onClick = onConfirm,
                enabled = confirmEnabled,
                rejectObscuredTouches = true
            )
        },
        dismissButton = {
            NofyTextButton(
                text = dismissLabel,
                onClick = onDismiss,
                rejectObscuredTouches = true
            )
        }
    )
}

@Composable
private fun DialogFormBody(
    message: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NofyText(text = message)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun NofyFormDialogPreview() {
    NofyTheme {
        NofyFormDialog(
            title = "Re-enable biometric login",
            message = "Confirm your current password and complete biometric authentication.",
            confirmLabel = "Continue",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
            content = {
                NofyPasswordField(
                    value = "",
                    onValueChange = {},
                    label = "Current Password"
                )
            }
        )
    }
}
