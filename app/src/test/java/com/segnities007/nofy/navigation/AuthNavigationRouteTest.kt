package com.segnities007.nofy.navigation

import com.segnities007.login.api.LoginRoute
import com.segnities007.note.api.NoteRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthNavigationRouteTest {
    @Test
    fun resolveInitialRoute_returnsSignUpWhenNotRegistered() {
        assertEquals(
            LoginRoute.SignUp,
            resolveInitialRoute(
                isRegistered = false,
                isLocked = true
            )
        )
    }

    @Test
    fun resolveInitialRoute_returnsLoginWhenRegisteredAndLocked() {
        assertEquals(
            LoginRoute.Login,
            resolveInitialRoute(
                isRegistered = true,
                isLocked = true
            )
        )
    }

    @Test
    fun resolveInitialRoute_returnsNotesWhenRegisteredAndUnlocked() {
        assertEquals(
            NoteRoute.NoteList,
            resolveInitialRoute(
                isRegistered = true,
                isLocked = false
            )
        )
    }

    @Test
    fun resolveForcedRoute_returnsNullWhenRegisteredAndUnlocked() {
        assertNull(
            resolveForcedRoute(
                isRegistered = true,
                isLocked = false
            )
        )
    }
}
