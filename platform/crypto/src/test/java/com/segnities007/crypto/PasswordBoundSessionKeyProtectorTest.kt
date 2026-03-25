package com.segnities007.crypto

import java.security.MessageDigest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PasswordBoundSessionKeyProtectorTest {
    private val protector = PasswordBoundSessionKeyProtector(
        sessionKeyDeriver = SessionKeyDeriver { password, salt ->
            MessageDigest.getInstance("SHA-256").digest(password + salt)
        }
    )

    @Test
    fun createAndUnwrap_roundTripsSessionKey() {
        val password = "very-long vault password".toByteArray()

        val created = protector.create(password)
        val unwrapped = protector.unwrap(password, created.wrappedState)

        assertArrayEquals(created.sessionKey, unwrapped)
        unwrapped.fill(0)
        created.sessionKey.fill(0)
        password.fill(0)
    }

    @Test(expected = Exception::class)
    fun unwrap_withDifferentPassword_fails() {
        val password = "very-long vault password".toByteArray()
        val wrongPassword = "different vault password".toByteArray()

        val created = protector.create(password)
        try {
            protector.unwrap(wrongPassword, created.wrappedState)
        } finally {
            created.sessionKey.fill(0)
            password.fill(0)
            wrongPassword.fill(0)
        }
    }

    @Test
    fun rewrap_changesWrappedPayloadButPreservesSessionKey() {
        val currentPassword = "current vault password".toByteArray()
        val newPassword = "new and longer vault password".toByteArray()

        val created = protector.create(currentPassword)
        val rewrapped = protector.rewrap(
            currentPassword = currentPassword,
            newPassword = newPassword,
            wrappedState = created.wrappedState
        )

        assertArrayEquals(created.sessionKey, rewrapped.sessionKey)
        assertFalse(created.wrappedState.wrappedKey.contentEquals(rewrapped.wrappedState.wrappedKey))
        assertNotEquals(
            created.wrappedState.iv.contentToString(),
            rewrapped.wrappedState.iv.contentToString()
        )

        created.sessionKey.fill(0)
        rewrapped.sessionKey.fill(0)
        currentPassword.fill(0)
        newPassword.fill(0)
    }
}
