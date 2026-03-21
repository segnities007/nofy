package com.segnities007.setting.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.chip.NofyChoiceChip
import com.segnities007.designsystem.atom.divider.NofyDivider
import com.segnities007.designsystem.atom.slider.NofySlider
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.atom.toggle.NofySwitch
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.settings.MaxFontScale
import com.segnities007.settings.MinFontScale
import com.segnities007.settings.ThemeMode
import com.segnities007.setting.R

@Composable
internal fun AppearanceSettingsPage(
    themeMode: ThemeMode,
    fontScale: Float,
    onIntent: (SettingIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NofyCardSurface {
                NofyText(
                    text = stringResource(R.string.settings_theme_heading),
                    style = NofyThemeTokens.typography.titleLarge
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeModeRow(
                        leftThemeMode = ThemeMode.Light,
                        rightThemeMode = ThemeMode.Dark,
                        selectedThemeMode = themeMode,
                        onThemeSelected = { onIntent(SettingIntent.SelectThemeMode(it)) }
                    )
                    ThemeModeRow(
                        leftThemeMode = ThemeMode.GreenLight,
                        rightThemeMode = ThemeMode.GreenDark,
                        selectedThemeMode = themeMode,
                        onThemeSelected = { onIntent(SettingIntent.SelectThemeMode(it)) }
                    )
                }
            }
        }

        item {
            NofyCardSurface {
                NofyText(
                    text = stringResource(R.string.settings_font_size_heading),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyText(
                    text = stringResource(R.string.settings_font_size_value, (fontScale * 100).toInt()),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
                NofySlider(
                    value = fontScale,
                    onValueChange = { onIntent(SettingIntent.ChangeFontScale(it)) },
                    valueRange = MinFontScale..MaxFontScale
                )
            }
        }

        item {
            NofyCardSurface {
                NofyText(
                    text = stringResource(R.string.settings_language_heading),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyText(
                    text = stringResource(R.string.settings_language_body),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun SecuritySettingsPage(
    uiState: SettingState,
    onIntent: (SettingIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        onCheckedChange = { onIntent(SettingIntent.SetBiometricEnabled(it)) },
                        enabled = !uiState.isBiometricUpdating
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
                    value = uiState.currentPassword,
                    onValueChange = { onIntent(SettingIntent.ChangeCurrentPassword(it)) },
                    label = stringResource(R.string.settings_security_current_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyPasswordField(
                    value = uiState.newPassword,
                    onValueChange = { onIntent(SettingIntent.ChangeNewPassword(it)) },
                    label = stringResource(R.string.settings_security_new_password),
                    modifier = Modifier.fillMaxWidth()
                )
                NofyPasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = { onIntent(SettingIntent.ChangeConfirmPassword(it)) },
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
                    onClick = { onIntent(SettingIntent.SavePassword) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.canUpdatePassword
                )
            }
        }
    }
}

@Composable
internal fun AppSettingsPage(
    isResetting: Boolean,
    onIntent: (SettingIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NofyCardSurface {
                NofyText(
                    text = stringResource(R.string.settings_app_storage_heading),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyText(
                    text = stringResource(R.string.settings_app_storage_body),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            NofyCardSurface(
                containerColor = NofyThemeTokens.colorScheme.errorContainer
            ) {
                NofyText(
                    text = stringResource(R.string.settings_app_reset_heading),
                    style = NofyThemeTokens.typography.titleLarge,
                    color = NofyThemeTokens.colorScheme.onErrorContainer
                )
                NofyText(
                    text = stringResource(R.string.settings_app_reset_body),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onErrorContainer
                )
                NofyButton(
                    text = stringResource(R.string.settings_app_reset_button),
                    onClick = { onIntent(SettingIntent.ResetApp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isResetting
                )
            }
        }
    }
}

@Composable
private fun ThemeModeRow(
    leftThemeMode: ThemeMode,
    rightThemeMode: ThemeMode,
    selectedThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NofyChoiceChip(
            label = leftThemeMode.label(),
            selected = selectedThemeMode == leftThemeMode,
            onClick = { onThemeSelected(leftThemeMode) },
            modifier = Modifier.weight(1f)
        )
        NofyChoiceChip(
            label = rightThemeMode.label(),
            selected = selectedThemeMode == rightThemeMode,
            onClick = { onThemeSelected(rightThemeMode) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeMode.label(): String {
    return when (this) {
        ThemeMode.Light -> stringResource(R.string.settings_theme_light)
        ThemeMode.Dark -> stringResource(R.string.settings_theme_dark)
        ThemeMode.GreenLight -> stringResource(R.string.settings_theme_green_light)
        ThemeMode.GreenDark -> stringResource(R.string.settings_theme_green_dark)
    }
}
