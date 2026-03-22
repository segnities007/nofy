package com.segnities007.auth.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordPolicyTest {
    @Test
    fun shortPassword_returnsMinimumLengthViolation() {
        val violation = PasswordPolicy.violationOrNull("fourteen-chars")

        assertEquals(
            PasswordPolicyViolation.TooShort(PasswordPolicy.MinimumLength),
            violation
        )
    }

    @Test
    fun blockedPassword_returnsCommonViolation() {
        val violation = PasswordPolicy.violationOrNull("Password123")

        assertEquals(PasswordPolicyViolation.TooCommon, violation)
    }

    @Test
    fun longUniquePassword_isAccepted() {
        val violation = PasswordPolicy.violationOrNull("Sufficiently-Long local vault passphrase")

        assertNull(violation)
    }

    @Test
    fun repeatedCharacterPassword_isRejected() {
        val violation = PasswordPolicy.violationOrNull("aaaaaaaaaaaaaaa")

        assertEquals(PasswordPolicyViolation.TooCommon, violation)
    }

    @Test
    fun sequentialPassword_isRejected() {
        val violation = PasswordPolicy.violationOrNull("123456789012345")

        assertEquals(PasswordPolicyViolation.TooCommon, violation)
    }
}
