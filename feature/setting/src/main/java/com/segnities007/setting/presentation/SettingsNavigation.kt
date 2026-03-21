package com.segnities007.setting.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.navigation.Route
import com.segnities007.settings.UiSettingsRepository

fun EntryProviderScope<NavKey>.settingsEntry(
    authRepository: AuthRepository,
    uiSettingsRepository: UiSettingsRepository,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    entry<Route.Settings> {
        SettingsScreen(
            authRepository = authRepository,
            uiSettingsRepository = uiSettingsRepository,
            onNavigateBack = onNavigateBack,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}
