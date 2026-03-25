package com.segnities007.setting.presentation.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.segnities007.setting.presentation.viewmodel.VaultReceiveUiState
import com.segnities007.setting.presentation.viewmodel.VaultTransferReceiveViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun VaultTransferReceiveScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultTransferReceiveViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var importPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.startSession()
    }

    LaunchedEffect(uiState) {
        if (uiState is VaultReceiveUiState.Done) {
            onNavigateBack()
        }
    }

    NofyBrushedStackScreen(
        title = stringResource(R.string.settings_vault_receive_title),
        onNavigateBack = onNavigateBack,
        backContentDescription = stringResource(R.string.settings_cd_go_back),
        modifier = modifier,
    ) {
        when (val s = uiState) {
            VaultReceiveUiState.Starting -> NofySupportingText(
                text = stringResource(R.string.settings_vault_receive_starting),
                style = NofyThemeTokens.typography.bodyLarge,
            )

            is VaultReceiveUiState.Listening -> {
                NofySupportingText(text = stringResource(R.string.settings_vault_receive_scan_hint))
                Spacer(Modifier.height(NofySpacing.md))
                NofyText(
                    text = stringResource(R.string.settings_vault_pairing_on_receiver, s.pairingCode),
                    style = NofyThemeTokens.typography.titleLarge
                )
                Spacer(Modifier.height(NofySpacing.sm))
                NofySupportingText(
                    text = stringResource(R.string.settings_vault_pairing_receiver_hint),
                    style = NofyThemeTokens.typography.bodyMedium
                )
                Spacer(Modifier.height(NofySpacing.md))
                Image(
                    bitmap = s.qrBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.settings_vault_receive_qr_cd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NofySpacing.qrCodeSlotHeight)
                )
                Spacer(Modifier.height(NofySpacing.sm))
                NofySupportingText(
                    text = stringResource(R.string.settings_vault_receive_waiting),
                    style = NofyThemeTokens.typography.bodyLarge,
                )
            }

            is VaultReceiveUiState.AwaitingPassword -> {
                NofyText(
                    text = stringResource(R.string.settings_vault_receive_password_prompt),
                    style = NofyThemeTokens.typography.bodyMedium
                )
                Spacer(Modifier.height(NofySpacing.sm))
                NofyPasswordField(
                    value = importPassword,
                    onValueChange = { importPassword = it },
                    label = stringResource(R.string.settings_vault_password_label),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(NofySpacing.md))
                NofyButton(
                    text = stringResource(R.string.settings_vault_receive_import),
                    onClick = { viewModel.importWithPassword(importPassword) },
                    modifier = Modifier.fillMaxWidth(),
                    rejectObscuredTouches = true
                )
            }

            VaultReceiveUiState.Importing -> NofySupportingText(
                text = stringResource(R.string.settings_vault_importing),
                style = NofyThemeTokens.typography.bodyLarge,
            )

            VaultReceiveUiState.Done -> Unit

            is VaultReceiveUiState.Failed -> NofyText(
                text = s.message,
                style = NofyThemeTokens.typography.bodyLarge,
                color = NofyThemeTokens.colorScheme.error
            )
        }
    }
}
