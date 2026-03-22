package com.segnities007.login.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.login.api.LoginRoute
import com.segnities007.login.presentation.screen.LoginScreen
import com.segnities007.login.presentation.screen.RegisterScreen
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.note.api.NoteRoute

internal class LoginNavigationEntryInstaller : NavigationEntryInstaller {
    override fun install(
        scope: EntryProviderScope<NavKey>,
        navigator: AppNavigator
    ) {
        with(scope) {
            loginEntry(navigator)
        }
    }
}

private fun EntryProviderScope<NavKey>.loginEntry(
    navigator: AppNavigator
) {
    entry<LoginRoute.Login> {
        LoginScreen(
            onLoginSuccess = {
                navigator.replaceWith(NoteRoute.NoteList)
            }
        )
    }
    entry<LoginRoute.SignUp> {
        RegisterScreen(
            onRegisterSuccess = {
                navigator.replaceWith(LoginRoute.Login)
            }
        )
    }
}
