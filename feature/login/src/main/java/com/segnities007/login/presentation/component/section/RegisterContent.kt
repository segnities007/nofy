package com.segnities007.login.presentation.component.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.R
import com.segnities007.login.presentation.contract.RegisterIntent
import com.segnities007.login.presentation.contract.RegisterState
import com.segnities007.login.presentation.preview.previewRegisterState

@Composable
internal fun RegisterContent(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AuthScreenLayout(
        modifier = modifier,
        hero = {
            AuthHeroSection(
                title = stringResource(R.string.register_title),
                description = stringResource(R.string.register_body)
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
                            password = password,
                            confirmPassword = confirmPassword
                        )
                    )
                    password = ""
                    confirmPassword = ""
                },
                onIntent = onIntent
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
    onIntent: (RegisterIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
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
