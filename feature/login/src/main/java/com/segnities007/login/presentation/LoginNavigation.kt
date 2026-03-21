package com.segnities007.login.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.navigation.Route

fun EntryProviderScope<NavKey>.loginEntry(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    entry<Route.Login> {
        LoginScreen(
            authRepository = authRepository,
            onLoginSuccess = onLoginSuccess
        )
    }
    entry<Route.SignUp> {
        RegisterScreen(
            authRepository = authRepository,
            onRegisterSuccess = onRegisterSuccess
        )
    }
}
