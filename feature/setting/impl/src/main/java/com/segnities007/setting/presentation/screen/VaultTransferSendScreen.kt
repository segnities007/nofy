package com.segnities007.setting.presentation.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.template.NofyBrushedStackScreen
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.setting.R
import com.segnities007.setting.presentation.vault.VaultQrScanner
import com.segnities007.setting.presentation.viewmodel.VaultSendUiState
import com.segnities007.setting.presentation.viewmodel.VaultTransferSendViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun VaultTransferSendScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultTransferSendViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var exportPassword by remember { mutableStateOf("") }
    var cameraGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraGranted = granted }

    LaunchedEffect(uiState) {
        if (uiState is VaultSendUiState.Scanning && !cameraGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is VaultSendUiState.Done) {
            onNavigateBack()
        }
    }

    NofyBrushedStackScreen(
        title = stringResource(R.string.settings_vault_send_title),
        onNavigateBack = onNavigateBack,
        backContentDescription = stringResource(R.string.settings_cd_go_back),
        modifier = modifier,
    ) {
        when (val s = uiState) {
            VaultSendUiState.Idle, VaultSendUiState.Exporting -> {
                NofySupportingText(text = stringResource(R.string.settings_vault_send_intro))
                Spacer(Modifier.height(NofySpacing.sm))
                NofyPasswordField(
                    value = exportPassword,
                    onValueChange = { exportPassword = it },
                    label = stringResource(R.string.settings_vault_password_label),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(NofySpacing.md))
                NofyButton(
                    text = stringResource(R.string.settings_vault_send_prepare),
                    onClick = { viewModel.beginExport(exportPassword) },
                    enabled = uiState !is VaultSendUiState.Exporting && exportPassword.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    rejectObscuredTouches = true
                )
                if (uiState is VaultSendUiState.Exporting) {
                    Spacer(Modifier.height(NofySpacing.sm))
                    NofySupportingText(
                        text = stringResource(R.string.settings_vault_exporting),
                        style = NofyThemeTokens.typography.bodyLarge,
                    )
                }
            }

            is VaultSendUiState.Scanning -> {
                NofyText(
                    text = stringResource(R.string.settings_vault_send_scan_hint),
                    style = NofyThemeTokens.typography.bodyMedium
                )
                Spacer(Modifier.height(NofySpacing.sm))
                if (cameraGranted) {
                    VaultQrScanner(
                        onQrDecoded = viewModel::onQrDecoded,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NofySpacing.qrScannerSlotHeight)
                    )
                } else {
                    NofyText(
                        text = stringResource(R.string.settings_vault_camera_denied),
                        color = NofyThemeTokens.colorScheme.error,
                        style = NofyThemeTokens.typography.bodyMedium
                    )
                }
            }

            VaultSendUiState.Sending -> NofySupportingText(
                text = stringResource(R.string.settings_vault_sending),
                style = NofyThemeTokens.typography.bodyLarge,
            )

            VaultSendUiState.Done -> Unit

            is VaultSendUiState.Failed -> NofyText(
                text = s.message,
                style = NofyThemeTokens.typography.bodyLarge,
                color = NofyThemeTokens.colorScheme.error
            )
        }
    }
}
