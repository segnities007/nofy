package com.segnities007.auth.domain.policy

import java.util.Locale
import java.text.Normalizer

/** マスターパスワードの最小長・禁止パターンを判定するドメインポリシー。 */
object PasswordPolicy {
    const val MinimumLength = 15

    fun violationOrNull(password: String): PasswordPolicyViolation? {
        val normalized = normalize(password)
        val canonical = normalized.lowercase(Locale.ROOT)
        if (canonical in blockedPasswords ||
            canonical.isRepeatedCharacterPassword() ||
            canonical.isSimpleSequentialPassword()
        ) {
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

    private fun String.isRepeatedCharacterPassword(): Boolean {
        return length >= MinimumLength && all { it == firstOrNull() }
    }

    private fun String.isSimpleSequentialPassword(): Boolean {
        if (length < MinimumLength) {
            return false
        }

        return hasStep(1) || hasStep(-1)
    }

    private fun String.hasStep(step: Int): Boolean {
        return zipWithNext().all { (current, next) ->
            next.code - current.code == step
        }
    }

    private val blockedPasswords = setOf(
        "000000",
        "00000000",
        "000000000000000",
        "111111",
        "11111111",
        "111111111111111",
        "123456",
        "12345678",
        "123456789",
        "1234567890",
        "123456789012345",
        "123123123123123",
        "1q2w3e4r",
        "1q2w3e4r5t6y7u8i",
        "abcdef",
        "abcdefghijklmno",
        "admin",
        "admin123",
        "adminadminadmin",
        "changeme",
        "dragon",
        "football",
        "letmein",
        "iloveyou",
        "loginloginlogin",
        "monkey",
        "nofy",
        "nofy123",
        "nofy1234567890",
        "nofynofynofy",
        "password1",
        "password12",
        "passw0rd",
        "password",
        "password123",
        "password1234",
        "password12345",
        "passwordpassword",
        "princess",
        "qwerty",
        "qwerty123",
        "qwertyqwertyqwe",
        "qwertyuiopasdfg",
        "secret123456789",
        "secret",
        "sunshine",
        "welcome123456789",
        "welcome"
    )
}

/**
 * [PasswordPolicy] に照らしたパスワードの不備（短すぎる・ありふれている等）。
 */
sealed interface PasswordPolicyViolation {
    /** [PasswordPolicy.MinimumLength] 未満。 */
    data class TooShort(val minimumLength: Int) : PasswordPolicyViolation

    /** 単純・よくあるパスワードや連続・同一文字パターン。 */
    data object TooCommon : PasswordPolicyViolation
}
