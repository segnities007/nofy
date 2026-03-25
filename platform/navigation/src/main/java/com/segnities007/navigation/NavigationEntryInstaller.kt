package com.segnities007.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * 機能モジュールが `EntryProviderScope` に自身の [NavKey] エントリを登録するための SAM。
 */
fun interface NavigationEntryInstaller {
    /**
     * [scope] に自モジュールの画面エントリを登録し、[navigator] で遷移できるようにする。
     */
    fun install(
        scope: EntryProviderScope<NavKey>,
        navigator: AppNavigator
    )
}
