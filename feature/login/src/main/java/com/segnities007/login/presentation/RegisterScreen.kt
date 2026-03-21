package com.segnities007.login.presentation

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.biometric.BiometricAuthenticator
import com.segnities007.crypto.BiometricCipher
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.logo.NofyLogo
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.login.R
import kotlinx.coroutines.flow.Flow

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.current

    val biometricAuthenticator = remember(activity) {
        (activity as? androidx.fragment.app.FragmentActivity)?.let { BiometricAuthenticator(it) }
    }
    val biometricCipher = remember { BiometricCipher() }
    val biometricHandler = rememberBiometricHandler(
        biometricAuthenticator = biometricAuthenticator,
        authenticatePrompt = BiometricPromptContent(
            title = stringResource(R.string.register_biometric_title),
            subtitle = stringResource(R.string.register_biometric_subtitle),
            failureMessage = stringResource(R.string.biometric_failed)
        )
    )

    val viewModel: RegisterViewModel = viewModel(
        factory = registerViewModelFactory(
            authRepository = authRepository,
            biometricCipher = biometricCipher,
            biometricHandler = biometricHandler
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveRegisterEffects(
        effect = viewModel.effect,
        onRegisterSuccess = onRegisterSuccess
    )

    NofySurface(modifier = modifier.fillMaxSize()) {
        RegisterContent(
            uiState = uiState,
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
private fun ObserveRegisterEffects(
    effect: Flow<RegisterEffect>,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(effect, context) {
        effect.collect { handledEffect ->
            when (handledEffect) {
                is RegisterEffect.ShowToastRes -> showToast(context, handledEffect.messageRes)
                is RegisterEffect.NavigateToLogin -> {
                    handledEffect.messageRes?.let { messageRes ->
                        showToast(context, messageRes)
                    }
                    onRegisterSuccess()
                }
            }
        }
    }
}

private fun showToast(
    context: Context,
    @StringRes messageRes: Int
) {
    Toast.makeText(
        context,
        context.getString(messageRes),
        Toast.LENGTH_SHORT
    ).show()
}

@Composable
private fun RegisterContent(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                NofyLogo()
                NofyText(
                    text = stringResource(R.string.register_title),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyText(
                    text = stringResource(R.string.register_body),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
            }
            NofyCardSurface {
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
    }
}

@Composable
@Preview(showBackground = true)
private fun RegisterScreenPreview() {
    NofyTheme {
        NofySurface {
            RegisterContent(
                uiState = RegisterState(),
                onIntent = {}
            )
        }
    }
}
