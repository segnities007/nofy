package com.segnities007.localtransfer

/**
 * X25519 一時鍵（端末内のみ保持。QR には公開鍵のみ載せる）。
 */
data class LocalTransferKeyMaterial(
    val privateKeyEncoded: ByteArray,
    val publicKeyEncoded: ByteArray
) {
    companion object {
        fun generate(): LocalTransferKeyMaterial = LocalTransferCrypto.generateKeyPair()
    }
}
