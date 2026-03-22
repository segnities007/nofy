package com.segnities007.setting.presentation.component.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.fillMaxWidth
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.setting.R
import com.segnities007.setting.presentation.component.layout.SettingsSectionList

@Composable
internal fun AppSection(
    isResetting: Boolean,
    onOpenOpenSourceLicenses: () -> Unit,
    onRequestReset: () -> Unit
) {
    SettingsSectionList {
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
            NofyCardSurface {
                NofyText(
                    text = stringResource(R.string.settings_app_licenses_heading),
                    style = NofyThemeTokens.typography.titleLarge
                )
                NofyText(
                    text = stringResource(R.string.settings_app_licenses_body),
                    style = NofyThemeTokens.typography.bodyMedium,
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant
                )
                NofyButton(
                    text = stringResource(R.string.settings_app_licenses_button),
                    onClick = onOpenOpenSourceLicenses,
                    modifier = Modifier.fillMaxWidth()
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
                    onClick = onRequestReset,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isResetting,
                    rejectObscuredTouches = true
                )
            }
        }
    }
}

@NofyPreview
@Composable
private fun AppSectionPreview() {
    NofyPreviewSurface {
        AppSection(
            isResetting = false,
            onOpenOpenSourceLicenses = {},
            onRequestReset = {}
        )
    }
}
