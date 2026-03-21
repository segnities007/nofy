package com.segnities007.login.presentation.component.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
                onIntent = onIntent
            )
        }
    )
}

@Composable
private fun RegisterFormCard(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        NofyPasswordField(
            value = uiState.password,
            onValueChange = { onIntent(RegisterIntent.ChangePassword(it)) },
            label = stringResource(R.string.register_password_label),
            modifier = Modifier.fillMaxWidth()
        )
        NofyPasswordField(
            value = uiState.confirmPassword,
            onValueChange = { onIntent(RegisterIntent.ChangeConfirmPassword(it)) },
            label = stringResource(R.string.register_confirm_password_label),
            modifier = Modifier.fillMaxWidth()
        )
        NofyButton(
            text = stringResource(R.string.register_button_text),
            onClick = { onIntent(RegisterIntent.Register) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
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
