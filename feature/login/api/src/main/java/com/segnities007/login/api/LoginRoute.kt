package com.segnities007.login.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface LoginRoute : NavKey {
    @Serializable
    data object Login : LoginRoute

    @Serializable
    data object SignUp : LoginRoute
}
