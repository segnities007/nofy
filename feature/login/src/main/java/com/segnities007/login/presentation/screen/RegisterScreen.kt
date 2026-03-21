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
import com.segnities007.login.presentation.biometric.rememberLoginBiometricHandler
import com.segnities007.login.presentation.component.section.RegisterContent
import com.segnities007.login.presentation.contract.RegisterEffect
import com.segnities007.login.presentation.contract.RegisterState
import com.segnities007.login.presentation.preview.previewRegisterState
import com.segnities007.login.presentation.viewmodel.RegisterViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val biometricHandler = rememberLoginBiometricHandler(
        authenticatePrompt = BiometricPromptContent(
            title = stringResource(R.string.register_biometric_title),
            subtitle = stringResource(R.string.register_biometric_subtitle),
            failureMessage = stringResource(R.string.biometric_failed)
        )
    )

    val viewModel: RegisterViewModel = koinViewModel(
        parameters = { parametersOf(biometricHandler) }
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
                is RegisterEffect.ShowToastResArgs -> showToast(
                    context = context,
                    messageRes = handledEffect.messageRes,
                    formatArgs = handledEffect.formatArgs
                )
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

private fun showToast(
    context: Context,
    @StringRes messageRes: Int,
    formatArgs: List<Any>
) {
    Toast.makeText(
        context,
        context.getString(messageRes, *formatArgs.toTypedArray()),
        Toast.LENGTH_SHORT
    ).show()
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
