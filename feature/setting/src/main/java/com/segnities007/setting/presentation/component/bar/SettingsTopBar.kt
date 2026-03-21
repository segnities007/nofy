package com.segnities007.setting.presentation.component.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.bar.NofyFloatingTopBar
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.setting.R
import com.segnities007.setting.presentation.contract.SettingsSection

@Composable
internal fun SettingsTopBar(
    currentSection: SettingsSection,
    onNavigateBack: () -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    NofyFloatingTopBar(
        modifier = modifier
    ) {
        NofyIconButton(
            imageVector = NofyIcons.Back,
            contentDescription = stringResource(R.string.settings_cd_back_to_notes),
            onClick = onNavigateBack
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NofyText(
                text = stringResource(R.string.settings_title),
                style = NofyThemeTokens.typography.titleLarge
            )
            NofyText(
                text = currentSection.label(),
                style = NofyThemeTokens.typography.bodySmall,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
        }

        NofyIconButton(
            imageVector = NofyIcons.Lock,
            contentDescription = stringResource(R.string.settings_cd_lock_app),
            onClick = onLock
        )
    }
}

@NofyPreview
@Composable
private fun SettingsTopBarPreview() {
    NofyPreviewSurface {
        SettingsTopBar(
            currentSection = SettingsSection.Security,
            onNavigateBack = {},
            onLock = {}
        )
    }
}

@Composable
private fun SettingsSection.label(): String {
    return when (this) {
        SettingsSection.Appearance -> stringResource(R.string.settings_tab_appearance)
        SettingsSection.Security -> stringResource(R.string.settings_tab_security)
        SettingsSection.App -> stringResource(R.string.settings_tab_app)
    }
}
