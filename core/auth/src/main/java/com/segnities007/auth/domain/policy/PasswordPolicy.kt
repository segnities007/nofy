package com.segnities007.auth.domain.policy

import java.util.Locale
import java.text.Normalizer

object PasswordPolicy {
    const val MinimumLength = 12

    fun violationOrNull(password: String): PasswordPolicyViolation? {
        val normalized = normalize(password)
        val canonical = normalized.lowercase(Locale.ROOT)
        if (canonical in blockedPasswords) {
            return PasswordPolicyViolation.TooCommon
        }

        return if (normalized.length < MinimumLength) {
            PasswordPolicyViolation.TooShort(MinimumLength)
        } else {
            null
        }
    }

    private fun normalize(password: String): String {
        return Normalizer.normalize(password, Normalizer.Form.NFC)
    }

    private val blockedPasswords = setOf(
        "000000",
        "00000000",
        "111111",
        "11111111",
        "123456",
        "12345678",
        "123456789",
        "1234567890",
        "1q2w3e4r",
        "abcdef",
        "admin",
        "letmein",
        "iloveyou",
        "nofy",
        "passw0rd",
        "password",
        "password123",
        "qwerty",
        "qwerty123",
        "secret",
        "welcome"
    )
}

sealed interface PasswordPolicyViolation {
    data class TooShort(val minimumLength: Int) : PasswordPolicyViolation
    data object TooCommon : PasswordPolicyViolation
}
