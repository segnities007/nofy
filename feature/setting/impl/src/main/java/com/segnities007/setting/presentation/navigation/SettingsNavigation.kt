package com.segnities007.setting.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.login.api.LoginRoute
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.setting.api.SettingsRoute
import com.segnities007.setting.presentation.screen.SettingsScreen
import com.segnities007.setting.presentation.screen.VaultTransferReceiveScreen
import com.segnities007.setting.presentation.screen.VaultTransferSendScreen

/** 設定およびボルト送受信ルートを Navigation3 に登録する。 */
internal class SettingNavigationEntryInstaller : NavigationEntryInstaller {
    override fun install(
        scope: EntryProviderScope<NavKey>,
        navigator: AppNavigator
    ) {
        with(scope) {
            settingsEntry(navigator)
        }
    }
}

private fun EntryProviderScope<NavKey>.settingsEntry(
    navigator: AppNavigator
) {
    entry<SettingsRoute.Settings> {
        SettingsScreen(
            onNavigateBack = navigator::pop,
            onNavigateToLogin = {
                navigator.replaceWith(LoginRoute.Login)
            },
            onNavigateToSignUp = {
                navigator.replaceWith(LoginRoute.SignUp)
            },
            onNavigateToVaultSend = {
                navigator.navigateTo(SettingsRoute.VaultTransferSend)
            },
            onNavigateToVaultReceive = {
                navigator.navigateTo(SettingsRoute.VaultTransferReceive)
            }
        )
    }
    entry<SettingsRoute.VaultTransferSend> {
        VaultTransferSendScreen(onNavigateBack = navigator::pop)
    }
    entry<SettingsRoute.VaultTransferReceive> {
        VaultTransferReceiveScreen(onNavigateBack = navigator::pop)
    }
}
