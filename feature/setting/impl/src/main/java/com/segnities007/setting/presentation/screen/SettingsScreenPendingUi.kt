package com.segnities007.setting.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.designsystem.util.showShortToast
import com.segnities007.setting.R
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingNavigationRequest
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingUserMessage

@Composable
internal fun ObservePendingSettingUi(
    uiState: SettingState,
    onIntent: (SettingIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.pendingUserMessage, context) {
        val message = uiState.pendingUserMessage ?: return@LaunchedEffect
        when (message) {
            is SettingUserMessage.ToastRes -> context.showShortToast(message.messageRes)
        }
        onIntent(SettingIntent.ConsumeUserMessage)
    }
    LaunchedEffect(uiState.pendingNavigation) {
        val pending = uiState.pendingNavigation ?: return@LaunchedEffect
        when (pending) {
            SettingNavigationRequest.ToLogin -> onNavigateToLogin()
            SettingNavigationRequest.ToSignUp -> onNavigateToSignUp()
        }
        onIntent(SettingIntent.ConsumeNavigation)
    }
}

internal fun resolveBiometricPasswordFailureMessage(result: Result<Unit>): Int {
    val error = result.exceptionOrNull()
    if (error is AuthException.InvalidPassword) return R.string.settings_toast_current_password_invalid
    if (error is AuthException.UntrustedEnvironment) return R.string.settings_toast_untrusted_environment
    if (error is AuthException.LockedOut) return R.string.settings_toast_too_many_attempts
    return R.string.settings_toast_biometric_update_failed
}
