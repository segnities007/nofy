package com.segnities007.localtransfer

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.Collections
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val HANDSHAKE_MAGIC = 0x4e4f4659
private const val PROTOCOL_VERSION: Byte = 3
private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_BITS = 128
private const val CHUNK_PLAINTEXT_MAX = 64 * 1024
private const val PUBLIC_KEY_LEN = 32

/** QR とバイナリ握手で共有するセッション検証バイト長（送信側 QR 必須・LAN 先着接続の緩和）。 */
const val SESSION_VERIFIER_BYTES: Int = 16

/**
 * 受信側画面に表示し、送信側が手入力するペアリングコード桁数（QR に含めない）。
 * ワイヤ上は UTF-8 の ASCII 数字 [0-9] 固定長。
 */
const val PAIRING_CODE_DIGIT_COUNT: Int = 8

/**
 * QR に載せる接続情報（受信側が生成）。
 */
data class LocalTransferQrPayload(
    val hostIpv4: String,
    val port: Int,
    val serverPublicKeyEncoded: ByteArray,
    val sessionVerifier: ByteArray
) {
    fun toJsonString(): String {
        val keyB64 = android.util.Base64.encodeToString(
            serverPublicKeyEncoded,
            android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
        )
        val tokenB64 = android.util.Base64.encodeToString(
            sessionVerifier,
            android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
        )
        return JSONObject()
            .put("v", 2)
            .put("h", hostIpv4)
            .put("p", port)
            .put("k", keyB64)
            .put("t", tokenB64)
            .toString()
    }

    companion object {
        fun parseJson(json: String): LocalTransferQrPayload {
            val obj = JSONObject(json)
            if (obj.optInt("v") != 2) {
                throw IllegalArgumentException("Unsupported QR version")
            }
            val host = obj.getString("h")
            val port = obj.getInt("p")
            val keyB64 = obj.getString("k")
            val tokenB64 = obj.getString("t")
            val key = android.util.Base64.decode(
                keyB64,
                android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
            )
            val verifier = android.util.Base64.decode(
                tokenB64,
                android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
            )
            require(key.size == PUBLIC_KEY_LEN)
            require(verifier.size == SESSION_VERIFIER_BYTES)
            return LocalTransferQrPayload(host, port, key, verifier)
        }
    }
}

/**
 * Wi‑Fi / LAN 想定で使えるローカル IPv4 を 1 つ返す（`wlan` / `en*` 等を優先）。
 */
fun preferredLocalIpv4Address(): String? {
    val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
    for (nif in interfaces) {
        if (!nif.isUp || nif.isLoopback) continue
        val name = nif.name.lowercase()
        val looksLikeWifiOrLan = name.startsWith("wlan") ||
            name.startsWith("wifi") ||
            name.startsWith("en") ||
            name == "eth0" ||
            name.startsWith("ap")
        if (!looksLikeWifiOrLan) continue
        for (addr in Collections.list(nif.inetAddresses)) {
            if (addr is Inet4Address && !addr.isLoopbackAddress) {
                return addr.hostAddress
            }
        }
    }
    for (nif in interfaces) {
        if (!nif.isUp || nif.isLoopback) continue
        for (addr in Collections.list(nif.inetAddresses)) {
            if (addr is Inet4Address && !addr.isLoopbackAddress) {
                return addr.hostAddress
            }
        }
    }
    return null
}

/** 数字のみを抽出し、[PAIRING_CODE_DIGIT_COUNT] 桁なら返す。 */
fun normalizedVaultPairingCodeOrNull(raw: String): String? {
    val digits = raw.filter { it.isDigit() }
    if (digits.length != PAIRING_CODE_DIGIT_COUNT) return null
    return digits
}

/** 同一 LAN 上でボルトファイルを X25519 + HKDF + AES-GCM で送受信する。 */
object LocalVaultTransfer {
    /**
     * 受信側: 1 クライアントだけ受け入れ、復号したボルトを [destination] に書き込む。
     */
    suspend fun receiveOnce(
        serverSocket: ServerSocket,
        serverKey: LocalTransferKeyMaterial,
        destination: File,
        timeoutMs: Long,
        expectedSessionVerifier: ByteArray,
        expectedPairingCodeUtf8: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(expectedSessionVerifier.size == SESSION_VERIFIER_BYTES)
            require(expectedPairingCodeUtf8.size == PAIRING_CODE_DIGIT_COUNT)
            serverSocket.reuseAddress = true
            serverSocket.soTimeout = timeoutMs.toInt().coerceIn(1, Int.MAX_VALUE)
            val socket = serverSocket.accept()
            socket.use { s ->
                s.soTimeout = timeoutMs.toInt().coerceIn(1, Int.MAX_VALUE)
                val input = DataInputStream(s.getInputStream())
                val output = DataOutputStream(s.getOutputStream())
                readClientHandshake(
                    input,
                    expectedSessionVerifier,
                    expectedPairingCodeUtf8
                )
                val clientPub = readPublicKey(input)
                output.write(serverKey.publicKeyEncoded)
                output.flush()
                val aesKey = LocalTransferCrypto.deriveAesKey(
                    LocalTransferCrypto.agreeSharedSecret(
                        serverKey.privateKeyEncoded,
                        clientPub
                    )
                )
                decryptStreamToFile(input, aesKey, destination)
            }
            Result.success(Unit)
        } catch (e: LocalTransferException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(LocalTransferException.IoFailed(e))
        }
    }

    /**
     * 送信側: [qr] の受信端末へ接続し、[vaultFile] を暗号化ストリームで送る。
     */
    suspend fun sendToPeer(
        qr: LocalTransferQrPayload,
        clientKey: LocalTransferKeyMaterial,
        vaultFile: File,
        timeoutMs: Long,
        pairingCodeUtf8: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(pairingCodeUtf8.size == PAIRING_CODE_DIGIT_COUNT)
            Socket().use { socket ->
                socket.soTimeout = timeoutMs.toInt().coerceIn(1, Int.MAX_VALUE)
                socket.connect(
                    InetSocketAddress(InetAddress.getByName(qr.hostIpv4), qr.port),
                    timeoutMs.toInt().coerceAtMost(Int.MAX_VALUE)
                )
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())
                writeClientHandshake(output, qr.sessionVerifier, pairingCodeUtf8)
                output.write(clientKey.publicKeyEncoded)
                output.flush()
                val serverPub = ByteArray(PUBLIC_KEY_LEN)
                input.readFully(serverPub)
                if (!serverPub.contentEquals(qr.serverPublicKeyEncoded)) {
                    return@withContext Result.failure(
                        LocalTransferException.HandshakeFailed("Server key mismatch")
                    )
                }
                val aesKey = LocalTransferCrypto.deriveAesKey(
                    LocalTransferCrypto.agreeSharedSecret(clientKey.privateKeyEncoded, serverPub)
                )
                encryptFileToStream(vaultFile, output, aesKey)
                Result.success(Unit)
            }
        } catch (e: LocalTransferException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(LocalTransferException.IoFailed(e))
        }
    }

    private fun writeClientHandshake(
        out: DataOutputStream,
        sessionVerifier: ByteArray,
        pairingCodeUtf8: ByteArray
    ) {
        require(sessionVerifier.size == SESSION_VERIFIER_BYTES)
        require(pairingCodeUtf8.size == PAIRING_CODE_DIGIT_COUNT)
        out.writeInt(HANDSHAKE_MAGIC)
        out.writeByte(PROTOCOL_VERSION.toInt())
        out.write(sessionVerifier)
        out.write(pairingCodeUtf8)
    }

    private fun readClientHandshake(
        input: DataInputStream,
        expectedSessionVerifier: ByteArray,
        expectedPairingCodeUtf8: ByteArray
    ) {
        val magic = input.readInt()
        if (magic != HANDSHAKE_MAGIC) {
            throw LocalTransferException.HandshakeFailed("Bad magic")
        }
        val ver = input.readByte()
        if (ver != PROTOCOL_VERSION) {
            throw LocalTransferException.HandshakeFailed("Bad version")
        }
        val token = ByteArray(SESSION_VERIFIER_BYTES)
        input.readFully(token)
        if (!MessageDigest.isEqual(expectedSessionVerifier, token)) {
            throw LocalTransferException.HandshakeFailed("Bad session token")
        }
        val pairing = ByteArray(PAIRING_CODE_DIGIT_COUNT)
        input.readFully(pairing)
        if (!MessageDigest.isEqual(expectedPairingCodeUtf8, pairing)) {
            throw LocalTransferException.PairingFailed()
        }
    }

    private fun readPublicKey(input: DataInputStream): ByteArray {
        val pub = ByteArray(PUBLIC_KEY_LEN)
        input.readFully(pub)
        return pub
    }

    private fun encryptFileToStream(file: File, out: DataOutputStream, aesKey: ByteArray) {
        val size = file.length()
        if (size <= 0L || size > 512L * 1024 * 1024) {
            throw LocalTransferException.InvalidPayload("Invalid file size")
        }
        out.writeLong(size)
        FileInputStream(file).use { fis ->
            var remaining = size
            var chunkIndex = 0L
            while (remaining > 0) {
                val take = min(CHUNK_PLAINTEXT_MAX.toLong(), remaining).toInt()
                val buf = ByteArray(take)
                var read = 0
                while (read < take) {
                    val n = fis.read(buf, read, take - read)
                    if (n < 0) throw LocalTransferException.InvalidPayload("Short read")
                    read += n
                }
                val nonce = nonceForChunk(chunkIndex)
                val cipher = Cipher.getInstance(AES_TRANSFORMATION)
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    SecretKeySpec(aesKey, "AES"),
                    GCMParameterSpec(GCM_TAG_BITS, nonce)
                )
                val ct = cipher.doFinal(buf)
                buf.fill(0)
                out.writeInt(ct.size)
                out.write(nonce)
                out.write(ct)
                remaining -= take
                chunkIndex++
            }
        }
        out.flush()
    }

    private fun decryptStreamToFile(input: DataInputStream, aesKey: ByteArray, destination: File) {
        val total = input.readLong()
        if (total < 0L || total > 512L * 1024 * 1024) {
            throw LocalTransferException.InvalidPayload("Invalid size")
        }
        var remaining = total
        FileOutputStream(destination).use { fos ->
            while (remaining > 0) {
                val ctLen = input.readInt()
                if (ctLen <= 0 || ctLen > CHUNK_PLAINTEXT_MAX + 256) {
                    throw LocalTransferException.InvalidPayload("Bad chunk")
                }
                val nonce = ByteArray(12)
                input.readFully(nonce)
                val ct = ByteArray(ctLen)
                input.readFully(ct)
                val cipher = Cipher.getInstance(AES_TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(aesKey, "AES"),
                    GCMParameterSpec(GCM_TAG_BITS, nonce)
                )
                val plain = cipher.doFinal(ct)
                ct.fill(0)
                fos.write(plain)
                remaining -= plain.size
                plain.fill(0)
            }
        }
        if (remaining != 0L) {
            throw LocalTransferException.InvalidPayload("Length mismatch")
        }
    }

    private fun nonceForChunk(index: Long): ByteArray {
        val b = ByteArray(12)
        var x = index
        for (i in 11 downTo 4) {
            b[i] = (x and 0xFF).toByte()
            x = x ushr 8
        }
        return b
    }
}
