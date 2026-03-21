package com.segnities007.login.presentation.screen

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.R
import com.segnities007.login.presentation.biometric.BiometricPromptContent
import com.segnities007.login.presentation.biometric.LoginBiometricHandler
import com.segnities007.login.presentation.biometric.isBiometricAvailable
import com.segnities007.login.presentation.biometric.rememberLoginBiometricHandler
import com.segnities007.login.presentation.component.section.LoginContent
import com.segnities007.login.presentation.contract.LoginEffect
import com.segnities007.login.presentation.contract.LoginIntent
import com.segnities007.login.presentation.contract.LoginState
import com.segnities007.login.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val biometricHandler = rememberLoginBiometricHandler(
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

    val viewModel: LoginViewModel = koinViewModel(
        parameters = { parametersOf(biometricHandler) }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveLoginEffects(
        effect = viewModel.effect,
        onLoginSuccess = onLoginSuccess
    )
    ObserveBiometricAvailability(
        biometricHandler = biometricHandler,
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
    biometricHandler: LoginBiometricHandler,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    LaunchedEffect(biometricHandler) {
        val isAvailable = biometricHandler.isBiometricAvailable()
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

@NofyPreview
@Composable
private fun LoginScreenPreview() {
    NofyPreviewSurface {
        LoginContent(
            uiState = LoginState(isBiometricAvailable = true),
            onIntent = {}
        )
    }
}
