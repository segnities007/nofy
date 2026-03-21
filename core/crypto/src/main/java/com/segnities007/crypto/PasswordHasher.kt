package com.segnities007.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.nio.charset.StandardCharsets
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import java.util.Base64

/**
 * Argon2を用いたパスワードのハッシュ化と検証を行うクラス。
 * AGENTS.mdの原則に基づき、最新の安定版 API を使用。
 */
class PasswordHasher(
    private val passwordPepper: PasswordPepper
) {
    private val argon2 = Argon2Kt()
    private val secureRandom = SecureRandom()

    private val tCost = 3
    private val mCost = 65536
    private val parallelism = 4
    private val saltLength = 16

    fun hashPassword(password: String): String {
        val salt = ByteArray(saltLength)
        secureRandom.nextBytes(salt)
        val normalizedPassword = normalize(password)
        val passwordBytes = normalizedPassword.toByteArray(StandardCharsets.UTF_8)

        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = passwordBytes,
            salt = salt,
            tCostInIterations = tCost,
            mCostInKibibyte = mCost,
            parallelism = parallelism
        )

        val hashBytes = result.rawHash.toByteArray()
        val pepperedHash = passwordPepper.pepper(
            hash = hashBytes,
            salt = salt,
            tCost = tCost,
            mCost = mCost,
            parallelism = parallelism
        )
        val encodedSalt = Base64.getEncoder().encodeToString(salt)
        val encodedHash = Base64.getEncoder().encodeToString(pepperedHash)
        passwordBytes.fill(0)

        return listOf(
            HashVersion,
            tCost.toString(),
            mCost.toString(),
            parallelism.toString(),
            encodedSalt,
            encodedHash
        ).joinToString(separator = ":")
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 6) return false
        if (parts[0] != HashVersion) return false

        val tCost = parts[1].toIntOrNull() ?: return false
        val mCost = parts[2].toIntOrNull() ?: return false
        val parallelism = parts[3].toIntOrNull() ?: return false
        val salt = decodeBase64OrNull(parts[4]) ?: return false
        val expectedHash = decodeBase64OrNull(parts[5]) ?: return false
        val normalizedPassword = normalize(password)
        val passwordBytes = normalizedPassword.toByteArray(StandardCharsets.UTF_8)

        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = passwordBytes,
            salt = salt,
            tCostInIterations = tCost,
            mCostInKibibyte = mCost,
            parallelism = parallelism
        )

        val actualHash = passwordPepper.pepper(
            hash = result.rawHash.toByteArray(),
            salt = salt,
            tCost = tCost,
            mCost = mCost,
            parallelism = parallelism
        )
        passwordBytes.fill(0)
        return MessageDigest.isEqual(actualHash, expectedHash)
    }

    private fun normalize(password: String): String {
        return Normalizer.normalize(password, Normalizer.Form.NFC)
    }

    private fun decodeBase64OrNull(value: String): ByteArray? {
        return runCatching {
            Base64.getDecoder().decode(value)
        }.getOrNull()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        val bytes = ByteArray(remaining())
        get(bytes)
        return bytes
    }

    private companion object {
        const val HashVersion = "v2"
    }
}
