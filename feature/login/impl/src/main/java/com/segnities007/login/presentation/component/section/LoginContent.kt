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
import com.segnities007.designsystem.atom.button.NofyTextButton
import com.segnities007.designsystem.molecule.layout.NofyFormFieldColumn
import com.segnities007.designsystem.molecule.layout.NofyLogoTitleBlock
import com.segnities007.designsystem.molecule.layout.NofyWeightedFormShell
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.R
import com.segnities007.login.presentation.contract.LoginIntent
import com.segnities007.login.presentation.contract.LoginState
import com.segnities007.login.presentation.preview.previewLoginState

@Composable
internal fun LoginContent(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }

    NofyWeightedFormShell(
        modifier = modifier,
        hero = {
            NofyLogoTitleBlock(title = stringResource(R.string.login_form_title))
        },
        form = {
            LoginFormCard(
                uiState = uiState,
                password = password,
                onPasswordChange = { password = it },
                onSubmitPassword = {
                    onIntent(LoginIntent.SubmitPassword(password))
                    password = ""
                },
                onIntent = onIntent
            )
        }
    )
}

@Composable
private fun LoginFormCard(
    uiState: LoginState,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmitPassword: () -> Unit,
    onIntent: (LoginIntent) -> Unit
) {
    NofyFormFieldColumn {
        NofyPasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.login_password_label),
            modifier = Modifier.fillMaxWidth()
        )
        NofyButton(
            text = stringResource(R.string.login_button_text),
            onClick = onSubmitPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            rejectObscuredTouches = true
        )
        if (uiState.isBiometricAvailable && uiState.isBiometricEnabled) {
            NofyTextButton(
                text = stringResource(R.string.biometric_title),
                onClick = { onIntent(LoginIntent.BiometricLogin) },
                enabled = !uiState.isLoading,
                rejectObscuredTouches = true
            )
        }
    }
}

@NofyPreview
@Composable
private fun LoginContentPreview() {
    NofyPreviewSurface {
        LoginContent(
            uiState = previewLoginState(),
            onIntent = {}
        )
    }
}
