package com.segnities007.nofy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.login.api.LoginRoute
import com.segnities007.note.api.NoteRoute
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

@Composable
fun NofyNavHost(
    authRepository: AuthRepository,
    entryInstallers: List<NavigationEntryInstaller>,
    modifier: Modifier = Modifier
) {
    val initialRoute by rememberInitialRoute(authRepository)
    val authNavigationState by rememberAuthNavigationState(authRepository)
    val currentRoute = initialRoute ?: return

    val backStack = rememberNavBackStack(currentRoute)
    val navigator = remember(backStack) { BackStackNavigator(backStack) }

    LaunchedEffect(authNavigationState) {
        val state = authNavigationState ?: return@LaunchedEffect
        resolveForcedRoute(
            isRegistered = state.isRegistered,
            isLocked = state.isLocked
        )?.let(backStack::replaceWith)
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = backStack::popIfPossible,
        transitionSpec = nofyTransitionSpec(),
        popTransitionSpec = nofyTransitionSpec(),
        predictivePopTransitionSpec = nofyPredictivePopTransitionSpec(),
        entryProvider = entryProvider {
            entryInstallers.forEach { installer ->
                installer.install(this, navigator)
            }
        }
    )
}

@Composable
private fun rememberInitialRoute(
    authRepository: AuthRepository
) = produceState<NavKey?>(initialValue = null, authRepository) {
    val state = authRepository.observeAuthNavigationState().first()
    value = resolveInitialRoute(
        isRegistered = state.isRegistered,
        isLocked = state.isLocked
    )
}

@Composable
private fun rememberAuthNavigationState(
    authRepository: AuthRepository
) = produceState<AuthNavigationState?>(initialValue = null, authRepository) {
    authRepository.observeAuthNavigationState().collect { value = it }
}

private class BackStackNavigator(
    private val backStack: MutableList<NavKey>
) : AppNavigator {
    override fun navigateTo(route: NavKey) {
        backStack.navigateTo(route)
    }

    override fun replaceWith(route: NavKey) {
        backStack.replaceWith(route)
    }

    override fun pop() {
        backStack.popIfPossible()
    }
}

private fun MutableList<NavKey>.navigateTo(route: NavKey) {
    if (lastOrNull() != route) {
        add(route)
    }
}

private fun MutableList<NavKey>.replaceWith(route: NavKey) {
    clear()
    add(route)
}

private fun MutableList<NavKey>.popIfPossible() {
    if (size > 1) {
        removeAt(lastIndex)
    }
}

private fun authGateRouteOrNull(
    isRegistered: Boolean,
    isLocked: Boolean
): NavKey? {
    if (!isRegistered) return LoginRoute.SignUp
    if (isLocked) return LoginRoute.Login
    return null
}

internal fun resolveInitialRoute(
    isRegistered: Boolean,
    isLocked: Boolean
): NavKey {
    return authGateRouteOrNull(isRegistered, isLocked) ?: NoteRoute.NoteList
}

internal fun resolveForcedRoute(
    isRegistered: Boolean,
    isLocked: Boolean
): NavKey? {
    return authGateRouteOrNull(isRegistered, isLocked)
}
