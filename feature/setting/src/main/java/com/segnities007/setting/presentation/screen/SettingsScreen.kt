package com.segnities007.setting.presentation.screen

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.setting.R
import com.segnities007.setting.presentation.biometric.SettingBiometricEnrollmentResult
import com.segnities007.setting.presentation.biometric.SettingBiometricPromptContent
import com.segnities007.setting.presentation.biometric.rememberSettingBiometricEnrollmentController
import com.segnities007.setting.presentation.component.dialog.SettingsBiometricEnrollmentDialog
import com.segnities007.setting.presentation.component.layout.SettingsScaffold
import com.segnities007.setting.presentation.contract.SettingEffect
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.preview.previewSettingState
import com.segnities007.setting.presentation.viewmodel.SettingViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingViewModel = koinViewModel()
    val authRepository = koinInject<AuthRepository>()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val biometricEnrollmentController = rememberSettingBiometricEnrollmentController(
        promptContent = SettingBiometricPromptContent(
            title = stringResource(R.string.settings_biometric_dialog_title),
            subtitle = stringResource(R.string.settings_biometric_dialog_body),
            unavailableMessage = stringResource(R.string.settings_toast_biometric_unavailable)
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isBiometricDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isBiometricActionInProgress by rememberSaveable { mutableStateOf(false) }

    ObserveSettingEffects(
        effect = viewModel.effect,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToSignUp = onNavigateToSignUp
    )

    NofySurface(modifier = modifier.fillMaxSize()) {
        SettingsScaffold(
            uiState = uiState,
            onIntent = viewModel::onIntent,
            onNavigateBack = onNavigateBack,
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            isBiometricBusy = isBiometricActionInProgress,
            canUpdatePassword = currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                !uiState.isPasswordUpdating,
            onCurrentPasswordChange = { currentPassword = it },
            onNewPasswordChange = { newPassword = it },
            onConfirmPasswordChange = { confirmPassword = it },
            onEnableBiometric = {
                isBiometricDialogVisible = true
            },
            onDisableBiometric = {
                scope.launch {
                    isBiometricActionInProgress = true
                    val result = authRepository.setBiometricEnabled(false)
                    isBiometricActionInProgress = false
                    val messageRes = if (result.isSuccess) {
                        R.string.settings_toast_biometric_updated
                    } else {
                        R.string.settings_toast_biometric_update_failed
                    }
                    showToast(context, messageRes)
                }
            },
            onSavePassword = {
                viewModel.onIntent(
                    SettingIntent.SavePassword(
                        currentPassword = currentPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                )
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
            }
        )
    }

    if (isBiometricDialogVisible) {
        SettingsBiometricEnrollmentDialog(
            isLoading = isBiometricActionInProgress,
            onConfirm = { password ->
                scope.launch {
                    isBiometricActionInProgress = true
                    val verification = authRepository.verifyPassword(password)
                    if (verification.isFailure) {
                        isBiometricActionInProgress = false
                        isBiometricDialogVisible = false
                        showToast(
                            context,
                            resolveBiometricPasswordFailureMessage(verification)
                        )
                        return@launch
                    }

                    when (val result = biometricEnrollmentController.enroll(password)) {
                        is SettingBiometricEnrollmentResult.Success -> {
                            val saveResult = authRepository.saveBiometricSecret(
                                encryptedSecret = result.secret.encryptedSecret,
                                iv = result.secret.iv
                            )
                            val messageRes = if (saveResult.isSuccess) {
                                R.string.settings_toast_biometric_updated
                            } else {
                                R.string.settings_toast_biometric_update_failed
                            }
                            showToast(context, messageRes)
                        }

                        is SettingBiometricEnrollmentResult.Failure -> {
                            showToast(context, result.message)
                        }

                        SettingBiometricEnrollmentResult.CredentialUnavailable -> {
                            showToast(context, R.string.settings_toast_biometric_reenroll_required)
                        }
                    }

                    isBiometricActionInProgress = false
                    isBiometricDialogVisible = false
                }
            },
            onDismiss = {
                if (!isBiometricActionInProgress) {
                    isBiometricDialogVisible = false
                }
            }
        )
    }
}

@Composable
private fun ObserveSettingEffects(
    effect: Flow<SettingEffect>,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(effect, context) {
        effect.collect { handledEffect ->
            when (handledEffect) {
                is SettingEffect.ShowToastRes -> showToast(context, handledEffect.messageRes)
                SettingEffect.NavigateToLogin -> onNavigateToLogin()
                SettingEffect.NavigateToSignUp -> onNavigateToSignUp()
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
    message: String
) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun resolveBiometricPasswordFailureMessage(result: Result<Unit>): Int {
    return when (val error = result.exceptionOrNull()) {
        AuthException.InvalidPassword -> R.string.settings_toast_current_password_invalid
        AuthException.UntrustedEnvironment -> R.string.settings_toast_untrusted_environment
        is AuthException.LockedOut -> R.string.settings_toast_too_many_attempts
        else -> R.string.settings_toast_biometric_update_failed
    }
}

@NofyPreview
@Composable
private fun SettingsScreenPreview() {
    NofyPreviewSurface {
        SettingsScaffold(
            uiState = previewSettingState(),
            onIntent = {},
            onNavigateBack = {},
            currentPassword = "",
            newPassword = "",
            confirmPassword = "",
            isBiometricBusy = false,
            canUpdatePassword = false,
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onEnableBiometric = {},
            onDisableBiometric = {},
            onSavePassword = {}
        )
    }
}
