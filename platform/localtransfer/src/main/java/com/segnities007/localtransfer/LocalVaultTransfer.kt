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
import java.util.Collections
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val HANDSHAKE_MAGIC = 0x4e4f4659
private const val PROTOCOL_VERSION: Byte = 1
private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_BITS = 128
private const val CHUNK_PLAINTEXT_MAX = 64 * 1024
private const val PUBLIC_KEY_LEN = 32

/**
 * QR に載せる接続情報（受信側が生成）。
 */
data class LocalTransferQrPayload(
    val hostIpv4: String,
    val port: Int,
    val serverPublicKeyEncoded: ByteArray
) {
    fun toJsonString(): String {
        val keyB64 = android.util.Base64.encodeToString(
            serverPublicKeyEncoded,
            android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
        )
        return """{"v":1,"h":"$hostIpv4","p":$port,"k":"$keyB64"}"""
    }

    companion object {
        fun parseJson(json: String): LocalTransferQrPayload {
            val obj = org.json.JSONObject(json)
            if (obj.optInt("v") != 1) {
                throw IllegalArgumentException("Unsupported QR version")
            }
            val host = obj.getString("h")
            val port = obj.getInt("p")
            val keyB64 = obj.getString("k")
            val key = android.util.Base64.decode(
                keyB64,
                android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
            )
            require(key.size == PUBLIC_KEY_LEN)
            return LocalTransferQrPayload(host, port, key)
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

/** 同一 LAN 上でボルトファイルを X25519 + HKDF + AES-GCM で送受信する。 */
object LocalVaultTransfer {
    /**
     * 受信側: 1 クライアントだけ受け入れ、復号したボルトを [destination] に書き込む。
     */
    suspend fun receiveOnce(
        serverSocket: ServerSocket,
        serverKey: LocalTransferKeyMaterial,
        destination: File,
        timeoutMs: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            serverSocket.reuseAddress = true
            serverSocket.soTimeout = timeoutMs.toInt().coerceIn(1, Int.MAX_VALUE)
            val socket = serverSocket.accept()
            socket.use { s ->
                s.soTimeout = timeoutMs.toInt().coerceIn(1, Int.MAX_VALUE)
                val input = DataInputStream(s.getInputStream())
                val output = DataOutputStream(s.getOutputStream())
                readClientHandshake(input)
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
        timeoutMs: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.soTimeout = timeoutMs.toInt().coerceIn(1, Int.MAX_VALUE)
                socket.connect(
                    InetSocketAddress(InetAddress.getByName(qr.hostIpv4), qr.port),
                    timeoutMs.toInt().coerceAtMost(Int.MAX_VALUE)
                )
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())
                writeClientHandshake(output)
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

    private fun writeClientHandshake(out: DataOutputStream) {
        out.writeInt(HANDSHAKE_MAGIC)
        out.writeByte(PROTOCOL_VERSION.toInt())
    }

    private fun readClientHandshake(input: DataInputStream) {
        val magic = input.readInt()
        if (magic != HANDSHAKE_MAGIC) {
            throw LocalTransferException.HandshakeFailed("Bad magic")
        }
        val ver = input.readByte()
        if (ver != PROTOCOL_VERSION) {
            throw LocalTransferException.HandshakeFailed("Bad version")
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
