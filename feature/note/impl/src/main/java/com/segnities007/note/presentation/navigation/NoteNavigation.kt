package com.segnities007.note.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.login.api.LoginRoute
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.note.api.NoteRoute
import com.segnities007.note.presentation.screen.NoteScreen
import com.segnities007.setting.api.SettingsRoute

/** [NoteRoute] を Navigation3 の [EntryProviderScope] に登録する。 */
internal class NoteNavigationEntryInstaller : NavigationEntryInstaller {
    override fun install(
        scope: EntryProviderScope<NavKey>,
        navigator: AppNavigator
    ) {
        with(scope) {
            noteEntry(navigator)
        }
    }
}

private fun EntryProviderScope<NavKey>.noteEntry(
    navigator: AppNavigator
) {
    entry<NoteRoute.NoteList> {
        NoteScreen(
            onNavigateToSettings = {
                navigator.navigateTo(SettingsRoute.Settings)
            },
            onNavigateToLogin = {
                navigator.replaceWith(LoginRoute.Login)
            }
        )
    }
}
