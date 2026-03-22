package com.segnities007.setting.presentation.screen

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.util.showShortToast
import com.segnities007.setting.R
import com.segnities007.setting.presentation.biometric.SettingBiometricEnrollmentController
import com.segnities007.setting.presentation.biometric.SettingBiometricEnrollmentResult
import com.segnities007.setting.presentation.biometric.SettingEncryptedBiometricSecret
import com.segnities007.setting.presentation.component.dialog.SettingsBiometricEnrollmentDialog
import com.segnities007.setting.presentation.contract.SettingIntent
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun SettingsBiometricEnrollmentDialogHost(
    visible: Boolean,
    isEnrollmentInProgress: Boolean,
    onIntent: (SettingIntent) -> Unit,
    controller: SettingBiometricEnrollmentController
) {
    val context = LocalContext.current
    val authRepository = koinInject<AuthRepository>()
    val scope = rememberCoroutineScope()
    if (!visible) return

    SettingsBiometricEnrollmentDialog(
        isLoading = isEnrollmentInProgress,
        onConfirm = { passwordBytes ->
            scope.launch {
                onIntent(SettingIntent.SetBiometricEnrollmentBusy(true))
                try {
                    runBiometricEnrollmentFlow(
                        passwordBytes = passwordBytes,
                        authRepository = authRepository,
                        controller = controller,
                        context = context
                    )
                } finally {
                    passwordBytes.fill(0)
                    onIntent(SettingIntent.SetBiometricEnrollmentBusy(false))
                    onIntent(SettingIntent.DismissBiometricEnrollmentDialog)
                }
            }
        },
        onDismiss = dismiss@{
            if (isEnrollmentInProgress) return@dismiss
            onIntent(SettingIntent.DismissBiometricEnrollmentDialog)
        }
    )
}

private suspend fun runBiometricEnrollmentFlow(
    passwordBytes: ByteArray,
    authRepository: AuthRepository,
    controller: SettingBiometricEnrollmentController,
    context: Context
) {
    val verification = authRepository.verifyPassword(
        String(passwordBytes, StandardCharsets.UTF_8)
    )
    if (verification.isFailure) {
        context.showShortToast(resolveBiometricPasswordFailureMessage(verification))
        return
    }

    when (val result = controller.enroll(passwordBytes)) {
        is SettingBiometricEnrollmentResult.Failure -> {
            context.showShortToast(result.message)
        }

        SettingBiometricEnrollmentResult.CredentialUnavailable -> {
            context.showShortToast(R.string.settings_toast_biometric_reenroll_required)
        }

        is SettingBiometricEnrollmentResult.Success -> {
            saveBiometricSecretAndNotify(context, authRepository, result.secret)
        }
    }
}

private suspend fun saveBiometricSecretAndNotify(
    context: Context,
    authRepository: AuthRepository,
    secret: SettingEncryptedBiometricSecret
) {
    val saveResult = authRepository.saveBiometricSecret(
        encryptedSecret = secret.encryptedSecret,
        iv = secret.iv
    )
    context.showShortToast(messageResForBiometricSecretSave(saveResult))
}

private fun messageResForBiometricSecretSave(saveResult: Result<Unit>): Int {
    if (saveResult.isSuccess) return R.string.settings_toast_biometric_updated
    return R.string.settings_toast_biometric_update_failed
}
