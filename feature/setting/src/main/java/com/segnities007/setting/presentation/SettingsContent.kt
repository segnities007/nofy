package com.segnities007.setting.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults

@Composable
internal fun SettingsContent(
    uiState: SettingState,
    onIntent: (SettingIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    top = NofyFloatingBarDefaults.TopBarReservedSpace + 4.dp,
                    bottom = NofyFloatingBarDefaults.BottomBarReservedSpace + 12.dp
                )
        ) {
            Crossfade(
                targetState = uiState.currentSection,
                label = "settings-section"
            ) { section ->
                when (section) {
                    SettingsSection.Appearance -> AppearanceSettingsPage(
                        themeMode = uiState.themeMode,
                        fontScale = uiState.fontScale,
                        onIntent = onIntent
                    )

                    SettingsSection.Security -> SecuritySettingsPage(
                        uiState = uiState,
                        onIntent = onIntent
                    )

                    SettingsSection.App -> AppSettingsPage(
                        isResetting = uiState.isResetting,
                        onIntent = onIntent
                    )
                }
            }
        }

        SettingsTopBar(
            currentSection = uiState.currentSection,
            onNavigateBack = onNavigateBack,
            onLock = { onIntent(SettingIntent.Lock) }
        )

        SettingsBottomBar(
            currentSection = uiState.currentSection,
            onSectionSelected = { onIntent(SettingIntent.SelectSection(it)) }
        )
    }
}
