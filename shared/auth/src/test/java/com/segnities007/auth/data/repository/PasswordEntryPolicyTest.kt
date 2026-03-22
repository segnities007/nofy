package com.segnities007.auth.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordEntryPolicyTest {
    @Test
    fun firstFourFailures_doNotLock() {
        var state = PasswordEntryState()
        repeat(4) { index ->
            state = PasswordEntryPolicy.applyFailure(
                state = state,
                nowMillis = index.toLong()
            )
        }

        assertEquals(4, state.failedAttempts)
        assertNull(PasswordEntryPolicy.activeLockoutRemainingMillis(state, nowMillis = 4L))
    }

    @Test
    fun fifthFailure_locksForThirtySeconds() {
        var state = PasswordEntryState()
        repeat(5) {
            state = PasswordEntryPolicy.applyFailure(
                state = state,
                nowMillis = 1_000L
            )
        }

        assertEquals(5, state.failedAttempts)
        assertEquals(30_000L, PasswordEntryPolicy.activeLockoutRemainingMillis(state, 1_000L))
    }

    @Test
    fun tenthFailure_escalatesToSixtySeconds() {
        var state = PasswordEntryState()
        repeat(5) { step ->
            state = PasswordEntryPolicy.applyFailure(
                state = state,
                nowMillis = step.toLong()
            )
        }
        state = PasswordEntryPolicy.normalize(
            state = state,
            nowMillis = 30_001L
        )
        repeat(5) { step ->
            state = PasswordEntryPolicy.applyFailure(
                state = state,
                nowMillis = 30_001L + step
            )
        }

        assertEquals(10, state.failedAttempts)
        assertEquals(
            60_000L,
            PasswordEntryPolicy.activeLockoutRemainingMillis(state, 30_005L)
        )
    }

    @Test
    fun cleared_resetsFailureState() {
        val state = PasswordEntryPolicy.cleared()

        assertEquals(0, state.failedAttempts)
        assertEquals(0L, state.lockoutEndsAtMillis)
    }
}
