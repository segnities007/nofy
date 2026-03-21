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
import com.segnities007.designsystem.atom.button.NofyTextButton
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
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
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
            title = stringResource(R.string.biometric_title),
            subtitle = stringResource(R.string.biometric_subtitle),
            failureMessage = stringResource(R.string.biometric_failed)
        ),
        cryptoPrompt = BiometricPromptContent(
            title = stringResource(R.string.biometric_authorize_title),
            subtitle = stringResource(R.string.biometric_authorize_subtitle),
            failureMessage = stringResource(R.string.biometric_failed)
        )
    )

    val viewModel: LoginViewModel = viewModel(
        factory = loginViewModelFactory(
            authRepository = authRepository,
            biometricCipher = biometricCipher,
            biometricHandler = biometricHandler
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveLoginEffects(
        effect = viewModel.effect,
        onLoginSuccess = onLoginSuccess
    )
    ObserveBiometricAvailability(
        biometricAuthenticator = biometricAuthenticator,
        onAvailabilityChanged = { isAvailable ->
            viewModel.onIntent(LoginIntent.SetBiometricAvailability(isAvailable))
        }
    )

    NofySurface(modifier = modifier.fillMaxSize()) {
        LoginContent(
            uiState = uiState,
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
private fun ObserveLoginEffects(
    effect: Flow<LoginEffect>,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(effect, context) {
        effect.collect { handledEffect ->
            when (handledEffect) {
                LoginEffect.NavigateToNotes -> onLoginSuccess()
                is LoginEffect.ShowToastMessage -> showToast(context, handledEffect.message)
                is LoginEffect.ShowToastRes -> showToast(context, handledEffect.messageRes)
            }
        }
    }
}

@Composable
private fun ObserveBiometricAvailability(
    biometricAuthenticator: BiometricAuthenticator?,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    LaunchedEffect(biometricAuthenticator) {
        val isAvailable = biometricAuthenticator?.isBiometricAvailable() ?: false
        onAvailabilityChanged(isAvailable)
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

private fun showToast(
    context: Context,
    message: String
) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
private fun LoginContent(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit,
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
                    text = stringResource(R.string.login_form_title),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyText(
                    text = stringResource(R.string.login_form_body),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
            }

            NofyCardSurface {
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
                if (uiState.isBiometricAvailable) {
                    NofyTextButton(
                        text = stringResource(R.string.biometric_title),
                        onClick = { onIntent(LoginIntent.BiometricLogin) },
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    NofyTheme {
        NofySurface {
            LoginContent(
                uiState = LoginState(isBiometricAvailable = true),
                onIntent = {}
            )
        }
    }
}
