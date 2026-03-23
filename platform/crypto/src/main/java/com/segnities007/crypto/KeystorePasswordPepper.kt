package com.segnities007.crypto

import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Mac
import javax.crypto.SecretKey

/** Keystore 保持の HMAC 鍵で Argon2 ハッシュへ追加の [PasswordPepper] をかける。 */
class KeystorePasswordPepper : PasswordPepper {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun pepper(
        hash: ByteArray,
        salt: ByteArray,
        tCost: Int,
        mCost: Int,
        parallelism: Int
    ): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(secretKey())
        mac.update(VERSION_HEADER)
        mac.update(intToBytes(tCost))
        mac.update(intToBytes(mCost))
        mac.update(intToBytes(parallelism))
        mac.update(intToBytes(salt.size))
        mac.update(salt)
        mac.update(hash)
        return mac.doFinal()
    }

    private fun secretKey(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        return KeystoreHmacKeyFactory.generate(KEY_ALIAS)
    }

    private fun intToBytes(value: Int): ByteArray {
        return ByteBuffer.allocate(Int.SIZE_BYTES).putInt(value).array()
    }

    private companion object {
        const val KEY_ALIAS = "password_pepper_key_v1"
        const val HMAC_ALGORITHM = "HmacSHA256"
        val VERSION_HEADER = byteArrayOf(0x50, 0x50, 0x31)
    }
}
