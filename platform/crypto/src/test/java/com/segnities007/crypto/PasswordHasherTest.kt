package com.segnities007.crypto

import java.security.MessageDigest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    private val passwordHasher = PasswordHasher(
        passwordPepper = object : PasswordPepper {
            override fun pepper(
                hash: ByteArray,
                salt: ByteArray,
                tCost: Int,
                mCost: Int,
                parallelism: Int
            ): ByteArray {
                return MessageDigest.getInstance("SHA-256").digest(
                    hash + salt + tCost.toByte() + parallelism.toByte()
                )
            }
        },
        argonHash = { password, salt, tCost, mCost, parallelism ->
            MessageDigest.getInstance("SHA-256").digest(
                password +
                    salt +
                    tCost.toByte() +
                    mCost.toString().toByteArray() +
                    parallelism.toByte()
            )
        }
    )

    @Test
    fun hashAndVerify_roundTripSucceeds() {
        val password = "A sufficiently long passphrase"

        val storedHash = passwordHasher.hashPassword(password)

        assertTrue(passwordHasher.verifyPassword(password, storedHash))
    }

    @Test
    fun verifyPassword_normalizesUnicodeInput() {
        val composedPassword = "Caf\u00E9 vault password"
        val decomposedPassword = "Cafe\u0301 vault password"
        val storedHash = passwordHasher.hashPassword(composedPassword)

        assertTrue(passwordHasher.verifyPassword(decomposedPassword, storedHash))
    }

    @Test
    fun verifyPassword_rejectsMalformedStoredHash() {
        assertFalse(passwordHasher.verifyPassword("A sufficiently long passphrase", "invalid"))
    }
}
