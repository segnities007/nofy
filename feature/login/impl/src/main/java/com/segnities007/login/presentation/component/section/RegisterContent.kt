package com.segnities007.login.presentation.component.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.molecule.layout.NofyFormFieldColumn
import com.segnities007.designsystem.molecule.layout.NofyLogoTitleBlock
import com.segnities007.designsystem.molecule.layout.NofyWeightedFormShell
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.R
import com.segnities007.login.presentation.contract.RegisterIntent
import com.segnities007.login.presentation.contract.RegisterState
import com.segnities007.login.presentation.preview.previewRegisterState
import java.nio.charset.StandardCharsets

@Composable
internal fun RegisterContent(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    NofyWeightedFormShell(
        modifier = modifier,
        hero = {
            NofyLogoTitleBlock(
                title = stringResource(R.string.register_title),
                subtitle = stringResource(R.string.register_body),
            )
        },
        form = {
            RegisterFormCard(
                uiState = uiState,
                password = password,
                confirmPassword = confirmPassword,
                onPasswordChange = { password = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onSubmitRegistration = {
                    onIntent(
                        RegisterIntent.SubmitRegistration(
                            passwordBytes = password.toByteArray(StandardCharsets.UTF_8),
                            confirmPasswordBytes = confirmPassword.toByteArray(
                                StandardCharsets.UTF_8
                            )
                        )
                    )
                    password = ""
                    confirmPassword = ""
                },
            )
        }
    )
}

@Composable
private fun RegisterFormCard(
    uiState: RegisterState,
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitRegistration: () -> Unit,
) {
    NofyFormFieldColumn {
        NofyPasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.register_password_label),
            modifier = Modifier.fillMaxWidth()
        )
        NofyPasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(R.string.register_confirm_password_label),
            modifier = Modifier.fillMaxWidth()
        )
        NofyButton(
            text = stringResource(R.string.register_button_text),
            onClick = onSubmitRegistration,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            rejectObscuredTouches = true
        )
    }
}

@NofyPreview
@Composable
private fun RegisterContentPreview() {
    NofyPreviewSurface {
        RegisterContent(
            uiState = previewRegisterState(),
            onIntent = {}
        )
    }
}
