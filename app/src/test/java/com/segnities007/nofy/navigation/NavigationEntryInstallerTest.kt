package com.segnities007.nofy.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.segnities007.login.api.LoginRoute
import com.segnities007.login.di.loginFeatureModule
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.note.di.noteFeatureModule
import com.segnities007.setting.di.settingFeatureModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class NavigationEntryInstallerTest {
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loginRoute_isRegisteredByInstalledEntries() {
        val koin = startKoin {
            modules(
                loginFeatureModule,
                noteFeatureModule,
                settingFeatureModule
            )
        }.koin
        val installers = koin.getAll<NavigationEntryInstaller>()
        val directProvider = entryProvider<NavKey> {
            entry<LoginRoute.Login> { }
        }
        val provider = entryProvider<NavKey> {
            installers.forEach { installer ->
                installer.install(this, NoopNavigator)
            }
        }

        directProvider(LoginRoute.Login)
        assertEquals(3, installers.size)
        provider(LoginRoute.Login)
    }
}

private object NoopNavigator : AppNavigator {
    override fun navigateTo(route: NavKey) = Unit

    override fun replaceWith(route: NavKey) = Unit

    override fun pop() = Unit
}
