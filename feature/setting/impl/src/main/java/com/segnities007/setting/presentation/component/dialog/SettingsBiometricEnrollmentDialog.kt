package com.segnities007.setting.presentation.component.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.molecule.dialog.NofyFormDialog
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.setting.R
import java.nio.charset.StandardCharsets

@Composable
internal fun SettingsBiometricEnrollmentDialog(
    isLoading: Boolean,
    onConfirm: (ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }

    NofyFormDialog(
        title = stringResource(R.string.settings_biometric_dialog_title),
        message = stringResource(R.string.settings_biometric_dialog_body),
        confirmLabel = stringResource(R.string.settings_biometric_dialog_confirm),
        dismissLabel = stringResource(R.string.settings_biometric_dialog_dismiss),
        confirmEnabled = currentPassword.isNotBlank() && !isLoading,
        onConfirm = {
            val passwordBytes = currentPassword.toByteArray(StandardCharsets.UTF_8)
            currentPassword = ""
            onConfirm(passwordBytes)
        },
        onDismiss = onDismiss
    ) {
        NofyPasswordField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = stringResource(R.string.settings_security_current_password)
        )
    }
}
