package com.segnities007.setting.presentation.component.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.divider.NofyDivider
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.atom.text.NofyTitleLargeText
import com.segnities007.designsystem.atom.toggle.NofySwitch
import com.segnities007.designsystem.molecule.layout.NofyCardSectionHeader
import com.segnities007.designsystem.molecule.layout.NofyLabelTrailingRow
import com.segnities007.designsystem.molecule.layout.NofyStackedFullWidthButtons
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.setting.R
import com.segnities007.setting.presentation.component.layout.SettingsSectionList
import com.segnities007.setting.presentation.contract.PasswordChangeDraft
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingsSection
import com.segnities007.setting.presentation.preview.previewSettingState

@Composable
internal fun SecuritySection(
    uiState: SettingState,
    passwordDraft: PasswordChangeDraft,
    onPasswordDraftChange: (PasswordChangeDraft) -> Unit,
    onIntent: (SettingIntent) -> Unit,
    onOpenVaultSend: () -> Unit,
    onOpenVaultReceive: () -> Unit,
) {
    val draft = passwordDraft
    val canUpdatePassword = draft.canSubmit(uiState.isPasswordUpdating)
    SettingsSectionList {
        item {
            NofyCardSurface {
                NofyCardSectionHeader(
                    title = stringResource(R.string.settings_vault_transfer_heading),
                    supporting = stringResource(R.string.settings_vault_transfer_body),
                )
                Spacer(modifier = Modifier.height(NofySpacing.md))
                NofyStackedFullWidthButtons {
                    NofyButton(
                        text = stringResource(R.string.settings_vault_send_title),
                        onClick = onOpenVaultSend,
                        modifier = Modifier.fillMaxWidth(),
                        rejectObscuredTouches = true
                    )
                    NofyButton(
                        text = stringResource(R.string.settings_vault_receive_title),
                        onClick = onOpenVaultReceive,
                        modifier = Modifier.fillMaxWidth(),
                        rejectObscuredTouches = true
                    )
                }
            }
        }

        item {
            NofyCardSurface {
                NofyLabelTrailingRow(
                    label = stringResource(R.string.settings_security_biometric),
                    trailingContent = {
                        NofySwitch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    onIntent(SettingIntent.OpenBiometricEnrollmentDialog)
                                } else {
                                    onIntent(SettingIntent.DisableBiometric)
                                }
                            },
                            enabled = !uiState.isBiometricSwitchBusy,
                            rejectObscuredTouches = true
                        )
                    }
                )
            }
        }

        item {
            NofyCardSurface {
                NofyTitleLargeText(text = stringResource(R.string.settings_security_password_heading))
                NofyDivider(modifier = Modifier.padding(vertical = NofySpacing.xs))
                NofyPasswordField(
                    value = draft.current,
                    onValueChange = { onPasswordDraftChange(draft.copy(current = it)) },
                    label = stringResource(R.string.settings_security_current_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyPasswordField(
                    value = draft.new,
                    onValueChange = { onPasswordDraftChange(draft.copy(new = it)) },
                    label = stringResource(R.string.settings_security_new_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyPasswordField(
                    value = draft.confirm,
                    onValueChange = { onPasswordDraftChange(draft.copy(confirm = it)) },
                    label = stringResource(R.string.settings_security_confirm_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofySupportingText(text = stringResource(R.string.settings_security_password_note))
                NofyButton(
                    text = stringResource(R.string.settings_security_update_password),
                    onClick = {
                        onIntent(
                            SettingIntent.SavePassword(
                                currentPassword = draft.current,
                                newPassword = draft.new,
                                confirmPassword = draft.confirm
                            )
                        )
                    },
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
            passwordDraft = PasswordChangeDraft(),
            onPasswordDraftChange = {},
            onIntent = {},
            onOpenVaultSend = {},
            onOpenVaultReceive = {}
        )
    }
}
