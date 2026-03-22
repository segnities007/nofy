package com.segnities007.navigation

import androidx.navigation3.runtime.NavKey

interface AppNavigator {
    fun navigateTo(route: NavKey)

    fun replaceWith(route: NavKey)

    fun pop()
}
