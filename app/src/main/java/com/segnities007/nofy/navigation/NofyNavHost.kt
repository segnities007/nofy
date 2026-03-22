package com.segnities007.nofy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.login.api.LoginRoute
import com.segnities007.note.api.NoteRoute
import com.segnities007.navigation.AppNavigator
import com.segnities007.navigation.NavigationEntryInstaller
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

@Composable
fun NofyNavHost(
    authRepository: AuthRepository,
    entryInstallers: List<NavigationEntryInstaller>,
    modifier: Modifier = Modifier
) {
    val initialRoute by rememberInitialRoute(authRepository)
    val authNavigationState by rememberAuthNavigationState(authRepository)
    val currentRoute = initialRoute ?: return // Wait for initialization

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

internal fun resolveInitialRoute(
    isRegistered: Boolean,
    isLocked: Boolean
): NavKey {
    return when {
        !isRegistered -> LoginRoute.SignUp
        isLocked -> LoginRoute.Login
        else -> NoteRoute.NoteList
    }
}

internal fun resolveForcedRoute(
    isRegistered: Boolean,
    isLocked: Boolean
): NavKey? {
    return when {
        !isRegistered -> LoginRoute.SignUp
        isLocked -> LoginRoute.Login
        else -> null
    }
}

@NofyPreview
@Composable
private fun NofyNavHostPreview() {
    NofyPreviewSurface {
        NofyNavHost(
            authRepository = PreviewAuthRepository,
            entryInstallers = listOf(previewNavigationEntryInstaller())
        )
    }
}

private fun previewNavigationEntryInstaller(): NavigationEntryInstaller {
    return NavigationEntryInstaller { scope, _ ->
        with(scope) {
            entry<LoginRoute.Login> {
                NofySurface {
                    NofyText(text = "Navigation preview")
                }
            }
        }
    }
}

private object PreviewAuthRepository : AuthRepository {
    override fun isRegistered(): Flow<Boolean> = flowOf(true)

    override fun isBiometricEnabled(): Flow<Boolean> = flowOf(false)

    override fun isLocked(): Flow<Boolean> = flowOf(true)

    override suspend fun verifyPassword(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun lock(): Result<Unit> = Result.success(Unit)

    override suspend fun unlock(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit> = Result.success(Unit)

    override suspend fun registerPassword(password: String): Result<Unit> = Result.success(Unit)

    override suspend fun saveBiometricSecret(
        encryptedSecret: ByteArray,
        iv: ByteArray
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>? = null

    override suspend fun clearBiometricSecret(): Result<Unit> = Result.success(Unit)

    override suspend fun reset(currentPassword: String): Result<Unit> = Result.success(Unit)

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> = Result.success(Unit)

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = Result.success(Unit)
}

private data class AuthNavigationState(
    val isRegistered: Boolean,
    val isLocked: Boolean
)

private fun AuthRepository.observeAuthNavigationState(): Flow<AuthNavigationState> {
    return combine(isRegistered(), isLocked()) { isRegistered, isLocked ->
        AuthNavigationState(
            isRegistered = isRegistered,
            isLocked = isLocked
        )
    }
}
