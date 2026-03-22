package com.segnities007.setting.presentation.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.setting.presentation.component.bar.SettingsBottomBar
import com.segnities007.setting.presentation.component.bar.SettingsTopBar
import com.segnities007.setting.presentation.component.dialog.SettingsResetDialog
import com.segnities007.setting.presentation.component.section.AppearanceSection
import com.segnities007.setting.presentation.component.section.AppSection
import com.segnities007.setting.presentation.component.section.SecuritySection
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingsSection
import com.segnities007.setting.presentation.preview.previewSettingState
import com.segnities007.setting.presentation.screen.SettingsPasswordDraftHolder
import com.segnities007.setting.presentation.screen.rememberSettingsPasswordDraftHolder

@Composable
internal fun SettingsScaffold(
    uiState: SettingState,
    onIntent: (SettingIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenOpenSourceLicenses: () -> Unit,
    passwordDraftHolder: SettingsPasswordDraftHolder,
    modifier: Modifier = Modifier
) {
    var isResetDialogVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        SettingsSectionCrossfade(
            uiState = uiState,
            onIntent = onIntent,
            onRequestReset = { isResetDialogVisible = true },
            onOpenOpenSourceLicenses = onOpenOpenSourceLicenses,
            passwordDraftHolder = passwordDraftHolder
        )

        AnimatedVisibility(
            visible = true,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            SettingsTopBar(
                currentSection = uiState.currentSection,
                onNavigateBack = onNavigateBack,
                onLock = { onIntent(SettingIntent.Lock) }
            )
        }

        AnimatedVisibility(
            visible = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            SettingsBottomBar(
                currentSection = uiState.currentSection,
                onSectionSelected = { onIntent(SettingIntent.SelectSection(it)) }
            )
        }

        if (isResetDialogVisible) {
            SettingsResetDialog(
                onConfirm = { pwd ->
                    isResetDialogVisible = false
                    onIntent(SettingIntent.ResetApp(pwd))
                },
                onDismiss = { isResetDialogVisible = false }
            )
        }
    }
}

@Composable
private fun SettingsSectionCrossfade(
    uiState: SettingState,
    onIntent: (SettingIntent) -> Unit,
    onRequestReset: () -> Unit,
    onOpenOpenSourceLicenses: () -> Unit,
    passwordDraftHolder: SettingsPasswordDraftHolder,
    modifier: Modifier = Modifier
) {
    Crossfade(
        modifier = modifier.fillMaxSize(),
        targetState = uiState.currentSection,
        label = "settings-section"
    ) { section ->
        when (section) {
            SettingsSection.Appearance -> AppearanceSection(
                themeMode = uiState.themeMode,
                fontScale = uiState.fontScale,
                onIntent = onIntent
            )

            SettingsSection.Security -> SecuritySection(
                uiState = uiState,
                passwordDraft = passwordDraftHolder.passwordDraft,
                onPasswordDraftChange = passwordDraftHolder::setPasswordDraft,
                onIntent = onIntent
            )

            SettingsSection.App -> AppSection(
                isResetting = uiState.isResetting,
                onOpenOpenSourceLicenses = onOpenOpenSourceLicenses,
                onRequestReset = onRequestReset
            )
        }
    }
}

@NofyPreview
@Composable
private fun SettingsScaffoldPreview() {
    val passwordDraftHolder = rememberSettingsPasswordDraftHolder()
    SettingsScaffold(
        uiState = previewSettingState(currentSection = SettingsSection.Appearance),
        onIntent = {},
        onNavigateBack = {},
        onOpenOpenSourceLicenses = {},
        passwordDraftHolder = passwordDraftHolder
    )
}
