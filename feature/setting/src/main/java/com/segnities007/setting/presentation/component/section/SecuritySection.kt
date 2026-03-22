package com.segnities007.setting.presentation.component.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.divider.NofyDivider
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.atom.toggle.NofySwitch
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.setting.R
import com.segnities007.setting.presentation.component.layout.SettingsSectionList
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingsSection
import com.segnities007.setting.presentation.preview.previewSettingState

@Composable
internal fun SecuritySection(
    uiState: SettingState,
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    isBiometricBusy: Boolean,
    canUpdatePassword: Boolean,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onEnableBiometric: () -> Unit,
    onDisableBiometric: () -> Unit,
    onSavePassword: () -> Unit
) {
    SettingsSectionList {
        item {
            NofyCardSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NofyText(
                        text = stringResource(R.string.settings_security_biometric),
                        modifier = Modifier.weight(1f),
                        style = NofyThemeTokens.typography.titleMedium
                    )
                    NofySwitch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                onEnableBiometric()
                            } else {
                                onDisableBiometric()
                            }
                        },
                        enabled = !isBiometricBusy,
                        rejectObscuredTouches = true
                    )
                }
            }
        }

        item {
            NofyCardSurface {
                NofyText(
                    text = stringResource(R.string.settings_security_password_heading),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyDivider(modifier = Modifier.padding(vertical = 4.dp))
                NofyPasswordField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = stringResource(R.string.settings_security_current_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyPasswordField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = stringResource(R.string.settings_security_new_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyPasswordField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = stringResource(R.string.settings_security_confirm_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyText(
                    text = stringResource(R.string.settings_security_password_note),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
                NofyButton(
                    text = stringResource(R.string.settings_security_update_password),
                    onClick = onSavePassword,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canUpdatePassword,
                    rejectObscuredTouches = true
                )
            }
        }
    }
}

@NofyPreview
@Composable
private fun SecuritySectionPreview() {
    NofyPreviewSurface {
        SecuritySection(
            uiState = previewSettingState(currentSection = SettingsSection.Security),
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
