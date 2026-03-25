package com.segnities007.setting.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.atom.surface.NofyFullscreenSurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.setting.R
import com.segnities007.setting.presentation.biometric.SettingBiometricPromptContent
import com.segnities007.setting.presentation.biometric.rememberSettingBiometricEnrollmentController
import com.segnities007.setting.presentation.component.layout.SettingsScaffold
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.navigation.openOpenSourceLicenses
import com.segnities007.setting.presentation.preview.previewSettingState
import com.segnities007.setting.presentation.viewmodel.SettingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToVaultSend: () -> Unit,
    onNavigateToVaultReceive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val passwordDraftHolder = rememberSettingsPasswordDraftHolder()
    val biometricEnrollmentController = rememberSettingBiometricEnrollmentController(
        promptContent = SettingBiometricPromptContent(
            title = stringResource(R.string.settings_biometric_dialog_title),
            subtitle = stringResource(R.string.settings_biometric_dialog_body),
            unavailableMessage = stringResource(R.string.settings_toast_biometric_unavailable)
        )
    )

    ObservePendingSettingUi(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToSignUp = onNavigateToSignUp
    )

    SettingsScreenContent(
        modifier = modifier,
        uiState = uiState,
        passwordDraftHolder = passwordDraftHolder,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onOpenVaultSend = onNavigateToVaultSend,
        onOpenVaultReceive = onNavigateToVaultReceive
    )

    SettingsBiometricEnrollmentDialogHost(
        visible = uiState.biometricEnrollmentDialogVisible,
        isEnrollmentInProgress = uiState.isBiometricEnrollmentInProgress,
        onIntent = viewModel::onIntent,
        controller = biometricEnrollmentController
    )
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier,
    uiState: SettingState,
    passwordDraftHolder: SettingsPasswordDraftHolder,
    onIntent: (SettingIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenVaultSend: () -> Unit,
    onOpenVaultReceive: () -> Unit
) {
    LaunchedEffect(uiState.passwordDraftClearNonce) {
        if (uiState.passwordDraftClearNonce != 0) {
            passwordDraftHolder.clearPasswordDraft()
        }
    }
    val context = LocalContext.current
    val licensesScreenTitle = stringResource(R.string.settings_app_licenses_screen_title)
    NofyFullscreenSurface(modifier = modifier) {
        SettingsScaffold(
            uiState = uiState,
            onIntent = onIntent,
            onNavigateBack = onNavigateBack,
            onOpenOpenSourceLicenses = {
                openOpenSourceLicenses(
                    context = context,
                    title = licensesScreenTitle
                )
            },
            onOpenVaultSend = onOpenVaultSend,
            onOpenVaultReceive = onOpenVaultReceive,
            passwordDraftHolder = passwordDraftHolder
        )
    }
}

@NofyPreview
@Composable
private fun SettingsScreenPreview() {
    NofyPreviewSurface {
        val passwordDraftHolder = rememberSettingsPasswordDraftHolder()
        SettingsScaffold(
            uiState = previewSettingState(),
            onIntent = {},
            onNavigateBack = {},
            onOpenOpenSourceLicenses = {},
            onOpenVaultSend = {},
            onOpenVaultReceive = {},
            passwordDraftHolder = passwordDraftHolder
        )
    }
}
