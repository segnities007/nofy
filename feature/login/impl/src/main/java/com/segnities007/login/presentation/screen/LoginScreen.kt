package com.segnities007.login.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.util.showShortToast
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.R
import com.segnities007.login.presentation.biometric.BiometricPromptContent
import com.segnities007.login.presentation.biometric.LoginBiometricHandler
import com.segnities007.login.presentation.biometric.isBiometricAvailable
import com.segnities007.login.presentation.biometric.rememberLoginBiometricHandler
import com.segnities007.login.presentation.component.section.LoginContent
import com.segnities007.login.presentation.contract.LoginIntent
import com.segnities007.login.presentation.contract.LoginNavigationRequest
import com.segnities007.login.presentation.contract.LoginState
import com.segnities007.login.presentation.contract.LoginUserMessage
import com.segnities007.login.presentation.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val biometricHandler = rememberLoginBiometricHandler(
        prompt = BiometricPromptContent(
            title = stringResource(R.string.biometric_authorize_title),
            subtitle = stringResource(R.string.biometric_authorize_subtitle),
            failureMessage = stringResource(R.string.biometric_failed)
        )
    )

    val viewModel: LoginViewModel = koinViewModel(
        parameters = { parametersOf(biometricHandler) }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObservePendingLoginUi(
        uiState = uiState,
        onIntent = viewModel::onIntent,
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
private fun ObservePendingLoginUi(
    uiState: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.pendingUserMessage, context) {
        when (val message = uiState.pendingUserMessage) {
            null -> return@LaunchedEffect
            is LoginUserMessage.ToastRes -> context.showShortToast(message.messageRes)
            is LoginUserMessage.ToastString -> context.showShortToast(message.message)
            is LoginUserMessage.Lockout -> showLockoutToast(context, message.remainingMillis)
        }
        onIntent(LoginIntent.ConsumeUserMessage)
    }
    LaunchedEffect(uiState.pendingNavigation) {
        when (uiState.pendingNavigation) {
            null -> return@LaunchedEffect
            LoginNavigationRequest.ToNotes -> onLoginSuccess()
        }
        onIntent(LoginIntent.ConsumeNavigation)
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

private fun showLockoutToast(
    context: Context,
    remainingMillis: Long
) {
    val remainingSeconds = ((remainingMillis + 999L) / 1_000L).coerceAtLeast(1L)
    context.showShortToast(
        R.string.login_error_too_many_attempts,
        listOf(remainingSeconds)
    )
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
