package com.segnities007.setting.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsRoute : NavKey {
    @Serializable
    data object Settings : SettingsRoute
}
