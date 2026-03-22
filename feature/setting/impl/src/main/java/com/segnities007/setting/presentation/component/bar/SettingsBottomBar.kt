package com.segnities007.setting.presentation.component.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.molecule.bar.NofyFloatingBottomBar
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.setting.R
import com.segnities007.setting.presentation.contract.SettingsSection

@Composable
internal fun SettingsBottomBar(
    currentSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    NofyFloatingBottomBar(
        modifier = modifier
    ) {
        SettingsBottomAction(
            icon = NofyIcons.Palette,
            contentDescription = stringResource(R.string.settings_tab_appearance),
            selected = currentSection == SettingsSection.Appearance,
            onClick = { onSectionSelected(SettingsSection.Appearance) }
        )
        SettingsBottomAction(
            icon = NofyIcons.Security,
            contentDescription = stringResource(R.string.settings_tab_security),
            selected = currentSection == SettingsSection.Security,
            onClick = { onSectionSelected(SettingsSection.Security) }
        )
        SettingsBottomAction(
            icon = NofyIcons.Tune,
            contentDescription = stringResource(R.string.settings_tab_app),
            selected = currentSection == SettingsSection.App,
            onClick = { onSectionSelected(SettingsSection.App) }
        )
    }
}

@Composable
private fun RowScope.SettingsBottomAction(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        NofyIconButton(
            imageVector = icon,
            contentDescription = contentDescription,
            onClick = onClick,
            selected = selected
        )
    }
}

@NofyPreview
@Composable
private fun SettingsBottomBarPreview() {
    NofyPreviewSurface {
        SettingsBottomBar(
            currentSection = SettingsSection.Appearance,
            onSectionSelected = {}
        )
    }
}
