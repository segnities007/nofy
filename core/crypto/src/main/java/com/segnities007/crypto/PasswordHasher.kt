package com.segnities007.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64

/**
 * Argon2を用いたパスワードのハッシュ化と検証を行うクラス。
 * AGENTS.mdの原則に基づき、最新の安定版 API を使用。
 */
class PasswordHasher {
    private val argon2 = Argon2Kt()
    private val secureRandom = SecureRandom()

    private val tCost = 3
    private val mCost = 65536
    private val parallelism = 4
    private val saltLength = 16

    fun hashPassword(password: String): String {
        val salt = ByteArray(saltLength)
        secureRandom.nextBytes(salt)

        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(),
            salt = salt,
            tCostInIterations = tCost,
            mCostInKibibyte = mCost,
            parallelism = parallelism
        )

        val encodedSalt = Base64.getEncoder().encodeToString(salt)
        val hashBytes = result.rawHash.toByteArray()
        val encodedHash = Base64.getEncoder().encodeToString(hashBytes)

        return "$encodedSalt:$encodedHash"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = Base64.getDecoder().decode(parts[0])
        val expectedHash = parts[1]

        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(),
            salt = salt,
            tCostInIterations = tCost,
            mCostInKibibyte = mCost,
            parallelism = parallelism
        )

        val actualHash = Base64.getEncoder().encodeToString(result.rawHash.toByteArray())
        return actualHash == expectedHash
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        val bytes = ByteArray(remaining())
        get(bytes)
        return bytes
    }
}
