package com.segnities007.login.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.atom.surface.NofyFullscreenSurface
import com.segnities007.designsystem.util.showShortToast
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.R
import com.segnities007.login.presentation.biometric.BiometricPromptContent
import com.segnities007.login.presentation.biometric.rememberLoginBiometricHandler
import com.segnities007.login.presentation.component.section.RegisterContent
import com.segnities007.login.presentation.contract.RegisterIntent
import com.segnities007.login.presentation.contract.RegisterNavigationRequest
import com.segnities007.login.presentation.contract.RegisterState
import com.segnities007.login.presentation.contract.RegisterUserMessage
import com.segnities007.login.presentation.preview.previewRegisterState
import com.segnities007.login.presentation.viewmodel.RegisterViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val biometricHandler = rememberLoginBiometricHandler(
        prompt = BiometricPromptContent(
            title = stringResource(R.string.register_biometric_title),
            subtitle = stringResource(R.string.register_biometric_subtitle),
            failureMessage = stringResource(R.string.biometric_failed)
        )
    )

    val viewModel: RegisterViewModel = koinViewModel(
        parameters = { parametersOf(biometricHandler) }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObservePendingRegisterUi(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onRegisterSuccess = onRegisterSuccess
    )

    NofyFullscreenSurface(modifier = modifier) {
        RegisterContent(
            uiState = uiState,
            onIntent = viewModel::onIntent
        )
    }
}

/** Toast とログインへのナビ（前置き Toast 含む）を消費する。 */
@Composable
private fun ObservePendingRegisterUi(
    uiState: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.pendingUserMessage, context) {
        when (val message = uiState.pendingUserMessage) {
            null -> return@LaunchedEffect
            is RegisterUserMessage.ToastRes -> context.showShortToast(message.messageRes)
            is RegisterUserMessage.ToastResArgs -> context.showShortToast(
                messageRes = message.messageRes,
                formatArgs = message.formatArgs
            )
        }
        onIntent(RegisterIntent.ConsumeUserMessage)
    }
    LaunchedEffect(uiState.pendingNavigation) {
        when (val nav = uiState.pendingNavigation) {
            null -> return@LaunchedEffect
            is RegisterNavigationRequest.ToLogin -> {
                nav.preludeToastRes?.let { context.showShortToast(it) }
                onRegisterSuccess()
            }
        }
        onIntent(RegisterIntent.ConsumeNavigation)
    }
}

@NofyPreview
@Composable
private fun RegisterScreenPreview() {
    NofyPreviewSurface {
        RegisterContent(
            uiState = previewRegisterState(),
            onIntent = {}
        )
    }
}
