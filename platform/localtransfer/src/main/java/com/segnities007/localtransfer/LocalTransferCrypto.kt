package com.segnities007.localtransfer

import java.security.SecureRandom
import java.security.Security
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.jce.provider.BouncyCastleProvider

/** ボルト転送プロトコル用の X25519・HKDF・鍵ペア生成（BouncyCastle）。 */
internal object LocalTransferCrypto {
    private val hkdfInfo = "nofy-vault-transfer-v1".toByteArray(Charsets.UTF_8)

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    fun generateKeyPair(): LocalTransferKeyMaterial {
        val kg = X25519KeyPairGenerator()
        kg.init(X25519KeyGenerationParameters(SecureRandom()))
        val kp = kg.generateKeyPair()
        val priv = kp.private as X25519PrivateKeyParameters
        val pub = kp.public as X25519PublicKeyParameters
        return LocalTransferKeyMaterial(
            privateKeyEncoded = priv.encoded,
            publicKeyEncoded = pub.encoded
        )
    }

    fun agreeSharedSecret(privateKeyEncoded: ByteArray, remotePublicKeyEncoded: ByteArray): ByteArray {
        val priv = X25519PrivateKeyParameters(privateKeyEncoded, 0)
        val remotePub = X25519PublicKeyParameters(remotePublicKeyEncoded, 0)
        val agreement = X25519Agreement()
        agreement.init(priv)
        val out = ByteArray(agreement.agreementSize)
        agreement.calculateAgreement(remotePub, out, 0)
        return out
    }

    fun deriveAesKey(sharedSecret: ByteArray): ByteArray {
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(sharedSecret, ByteArray(0), hkdfInfo))
        val key = ByteArray(32)
        hkdf.generateBytes(key, 0, 32)
        return key
    }
}

/** ローカル転送（ハンドシェイク・ペイロード・IO）の失敗。 */
sealed class LocalTransferException(message: String) : Exception(message) {
    /** マジックバージョン不一致などプロトコル上の不一致。 */
    class HandshakeFailed(message: String) : LocalTransferException(message)

    /** 復号・長さ・チャンク形式が不正。 */
    class InvalidPayload(message: String) : LocalTransferException(message)

    /** ソケット・ファイル等の低レベル失敗。 */
    class IoFailed(cause: Throwable) : LocalTransferException(cause.message ?: "IO failed") {
        init {
            initCause(cause)
        }
    }
}
