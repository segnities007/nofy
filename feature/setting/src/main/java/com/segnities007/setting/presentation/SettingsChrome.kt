package com.segnities007.setting.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.statusBarsPadding
import com.segnities007.designsystem.atom.button.NofyIconButton
import com.segnities007.designsystem.atom.icon.NofyIcon
import com.segnities007.designsystem.atom.icon.NofyIcons
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.molecule.bar.NofyFloatingBottomBar
import com.segnities007.designsystem.molecule.bar.NofyFloatingTopBar
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.setting.R

@Composable
internal fun BoxScope.SettingsTopBar(
    currentSection: SettingsSection,
    onNavigateBack: () -> Unit,
    onLock: () -> Unit
) {
    NofyFloatingTopBar(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
    ) {
        NofyIconButton(onClick = onNavigateBack) {
            NofyIcon(
                imageVector = NofyIcons.Back,
                contentDescription = stringResource(R.string.settings_cd_back_to_notes)
            )
        }

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

        NofyIconButton(onClick = onLock) {
            NofyIcon(
                imageVector = NofyIcons.Lock,
                contentDescription = stringResource(R.string.settings_cd_lock_app)
            )
        }
    }
}

@Composable
internal fun BoxScope.SettingsBottomBar(
    currentSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit
) {
    NofyFloatingBottomBar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
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
        Box(
            modifier = Modifier
                .clip(NofyThemeTokens.shapes.large)
                .background(
                    if (selected) {
                        NofyThemeTokens.colorScheme.secondaryContainer
                    } else {
                        NofyThemeTokens.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
                    }
                )
        ) {
            NofyIconButton(onClick = onClick) {
                NofyIcon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (selected) {
                        NofyThemeTokens.colorScheme.onSecondaryContainer
                    } else {
                        NofyThemeTokens.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
internal fun SettingsSection.label(): String {
    return when (this) {
        SettingsSection.Appearance -> stringResource(R.string.settings_tab_appearance)
        SettingsSection.Security -> stringResource(R.string.settings_tab_security)
        SettingsSection.App -> stringResource(R.string.settings_tab_app)
    }
}
