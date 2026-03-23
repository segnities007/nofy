package com.segnities007.setting.presentation.component.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.molecule.layout.NofyCardIntroActionsColumn
import com.segnities007.designsystem.molecule.layout.NofyCardSectionHeader
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
                NofyCardSectionHeader(
                    title = stringResource(R.string.settings_app_storage_heading),
                    supporting = stringResource(R.string.settings_app_storage_body),
                )
            }
        }

        item {
            NofyCardSurface {
                NofyCardIntroActionsColumn(
                    title = stringResource(R.string.settings_app_licenses_heading),
                    supporting = stringResource(R.string.settings_app_licenses_body),
                ) {
                    NofyButton(
                        text = stringResource(R.string.settings_app_licenses_button),
                        onClick = onOpenOpenSourceLicenses,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            NofyCardSurface(
                containerColor = NofyThemeTokens.colorScheme.errorContainer
            ) {
                NofyCardIntroActionsColumn(
                    title = stringResource(R.string.settings_app_reset_heading),
                    supporting = stringResource(R.string.settings_app_reset_body),
                    titleColor = NofyThemeTokens.colorScheme.onErrorContainer,
                    supportingColor = NofyThemeTokens.colorScheme.onErrorContainer,
                ) {
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
