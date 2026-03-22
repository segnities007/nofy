package com.segnities007.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun interface NavigationEntryInstaller {
    fun install(
        scope: EntryProviderScope<NavKey>,
        navigator: AppNavigator
    )
}
