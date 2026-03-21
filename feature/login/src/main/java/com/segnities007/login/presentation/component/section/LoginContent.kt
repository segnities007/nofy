package com.segnities007.login.presentation.component.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.button.NofyTextButton
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
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
    AuthScreenLayout(
        modifier = modifier,
        hero = {
            AuthHeroSection(
                title = stringResource(R.string.login_form_title)
            )
        },
        form = {
            LoginFormCard(
                uiState = uiState,
                onIntent = onIntent
            )
        }
    )
}

@Composable
private fun LoginFormCard(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AuthFormSpacing)
    ) {
        NofyPasswordField(
            value = uiState.password,
            onValueChange = { onIntent(LoginIntent.ChangePassword(it)) },
            label = stringResource(R.string.login_password_label),
            modifier = Modifier.fillMaxWidth()
        )
        NofyButton(
            text = stringResource(R.string.login_button_text),
            onClick = { onIntent(LoginIntent.Login) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )
        if (uiState.isBiometricAvailable && uiState.isBiometricEnabled) {
            NofyTextButton(
                text = stringResource(R.string.biometric_title),
                onClick = { onIntent(LoginIntent.BiometricLogin) },
                enabled = !uiState.isLoading
            )
        }
    }
}

private val AuthFormSpacing = 24.dp

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
