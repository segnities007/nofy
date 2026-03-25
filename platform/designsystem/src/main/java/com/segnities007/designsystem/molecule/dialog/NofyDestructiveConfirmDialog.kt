package com.segnities007.designsystem.molecule.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.button.NofyTextButton
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.atom.textfield.NofyTextField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyDestructiveConfirmDialog(
    title: String,
    body: String,
    warnings: List<String>,
    confirmationLabel: String,
    confirmationHint: String,
    confirmationValue: String,
    expectedConfirmation: String,
    confirmLabel: String,
    dismissLabel: String,
    confirmEnabled: Boolean = confirmationValue == expectedConfirmation,
    onConfirmationValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        NofyCardSurface(
            modifier = Modifier.fillMaxWidth(),
            containerColor = NofyThemeTokens.colorScheme.surfaceContainerHighest
        ) {
            NofyText(
                text = title,
                style = NofyThemeTokens.typography.titleLarge
            )
            NofyText(
                text = body,
                style = NofyThemeTokens.typography.bodyLarge,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(NofySpacing.md)) {
                warnings.forEach { warning ->
                    WarningRow(text = warning)
                }
            }
            NofyText(
                text = confirmationHint,
                style = NofyThemeTokens.typography.bodyMedium,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
            NofyTextField(
                value = confirmationValue,
                onValueChange = onConfirmationValueChange,
                label = confirmationLabel,
                singleLine = true,
                rejectObscuredTouches = true
            )
            extraContent()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                NofyTextButton(
                    text = dismissLabel,
                    onClick = onDismiss
                )
            }
            NofyButton(
                text = confirmLabel,
                onClick = onConfirm,
                enabled = confirmEnabled,
                rejectObscuredTouches = true
            )
        }
    }
}

@Composable
private fun WarningRow(
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NofySpacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = NofySpacing.sm)
                .background(
                    color = NofyThemeTokens.colorScheme.error,
                    shape = CircleShape
                )
                .padding(NofySpacing.xs)
        )
        NofyText(
            text = text,
            modifier = Modifier.weight(1f),
            style = NofyThemeTokens.typography.bodyMedium,
            color = NofyThemeTokens.colorScheme.onSurfaceVariant
        )
    }
}

@NofyPreview
@Composable
private fun NofyDestructiveConfirmDialogPreview() {
    NofyPreviewSurface {
        NofyDestructiveConfirmDialog(
            title = "Reset app?",
            body = "This permanently deletes encrypted data on this device.",
            warnings = listOf(
                "All notes will be removed.",
                "Passwords and biometric setup will be removed.",
                "You will return to the registration screen."
            ),
            confirmationLabel = "Type RESET to continue",
            confirmationHint = "Enter RESET exactly as shown.",
            confirmationValue = "",
            expectedConfirmation = "RESET",
            confirmLabel = "Reset app",
            dismissLabel = "Cancel",
            onConfirmationValueChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
