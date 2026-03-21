package com.segnities007.nofy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.segnities007.login.presentation.loginEntry
import com.segnities007.navigation.Route
import com.segnities007.nofy.di.NofyAppContainer
import com.segnities007.note.presentation.noteEntry
import com.segnities007.setting.presentation.settingsEntry
import kotlinx.coroutines.flow.first

@Composable
fun NofyNavHost(
    appContainer: NofyAppContainer,
    modifier: Modifier = Modifier
) {
    val initialRoute by rememberInitialRoute(appContainer)
    val currentRoute = initialRoute ?: return // Wait for initialization

    val backStack = rememberNavBackStack(currentRoute)
    
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = backStack::popIfPossible,
        entryProvider = entryProvider {
            loginEntry(
                authRepository = appContainer.authRepository,
                onLoginSuccess = {
                    backStack.replaceWith(Route.NoteList)
                },
                onRegisterSuccess = {
                    backStack.replaceWith(Route.Login)
                }
            )
            noteEntry(
                noteRepository = appContainer.noteRepository,
                authRepository = appContainer.authRepository,
                onNavigateToSettings = {
                    backStack.navigateTo(Route.Settings)
                },
                onNavigateToLogin = {
                    backStack.replaceWith(Route.Login)
                }
            )
            settingsEntry(
                authRepository = appContainer.authRepository,
                uiSettingsRepository = appContainer.uiSettingsRepository,
                onNavigateBack = backStack::popIfPossible,
                onNavigateToLogin = {
                    backStack.replaceWith(Route.Login)
                }
            )
        }
    )
}

@Composable
private fun rememberInitialRoute(
    appContainer: NofyAppContainer
) = produceState<Route?>(initialValue = null, appContainer) {
    val isRegistered = appContainer.authRepository.isRegistered().first()
    value = if (isRegistered) Route.Login else Route.SignUp
}

private fun MutableList<NavKey>.navigateTo(route: Route) {
    if (lastOrNull() != route) {
        add(route)
    }
}

private fun MutableList<NavKey>.replaceWith(route: Route) {
    clear()
    add(route)
}

private fun MutableList<NavKey>.popIfPossible() {
    if (size > 1) {
        removeAt(lastIndex)
    }
}
