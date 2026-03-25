package com.segnities007.note.data.transfer

import com.segnities007.crypto.ExportedVaultCryptoState
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.min

/**
 * マジック・バージョン・SQLCipher DB バイナリ・[ExportedVaultCryptoState] を 1 ストリームにまとめる。
 */
internal object VaultTransferBundleCodec {
    private val MAGIC = "NOFYB1".toByteArray(Charsets.US_ASCII)
    private const val VERSION = 1
    private const val MAX_DB_BYTES = 512L * 1024 * 1024

    fun packToStream(dbFile: File, crypto: ExportedVaultCryptoState, out: FileOutputStream) {
        val dos = DataOutputStream(out)
        dos.write(MAGIC)
        dos.writeInt(VERSION)
        val len = dbFile.length()
        require(len > 0 && len <= MAX_DB_BYTES)
        dos.writeLong(len)
        FileInputStream(dbFile).use { input ->
            var remaining = len
            val buffer = ByteArray(8192)
            while (remaining > 0) {
                val toRead = min(buffer.size.toLong(), remaining).toInt()
                var read = 0
                while (read < toRead) {
                    val n = input.read(buffer, read, toRead - read)
                    require(n >= 0) { "Unexpected EOF reading DB" }
                    if (n == 0) continue
                    read += n
                }
                dos.write(buffer, 0, toRead)
                remaining -= toRead
            }
        }
        require(crypto.salt.size in 1..256)
        require(crypto.iv.size in 1..256)
        require(crypto.wrappedSessionKey.isNotEmpty() && crypto.wrappedSessionKey.size <= 4096)
        dos.writeShort(crypto.salt.size)
        dos.write(crypto.salt)
        dos.writeShort(crypto.iv.size)
        dos.write(crypto.iv)
        dos.writeInt(crypto.wrappedSessionKey.size)
        dos.write(crypto.wrappedSessionKey)
        dos.flush()
    }

    fun unpackFromStream(input: DataInputStream, dbOut: File): ExportedVaultCryptoState {
        val magic = ByteArray(MAGIC.size)
        input.readFully(magic)
        require(magic.contentEquals(MAGIC)) { "Invalid vault bundle" }
        val ver = input.readInt()
        require(ver == VERSION)
        val dbLen = input.readLong()
        require(dbLen > 0 && dbLen <= MAX_DB_BYTES)
        FileOutputStream(dbOut).use { fos ->
            copyExact(input, fos, dbLen)
        }
        val saltLen = input.readUnsignedShort()
        require(saltLen in 1..256)
        val salt = ByteArray(saltLen)
        input.readFully(salt)
        val ivLen = input.readUnsignedShort()
        require(ivLen in 1..256)
        val iv = ByteArray(ivLen)
        input.readFully(iv)
        val wrappedLen = input.readInt()
        require(wrappedLen in 1..4096)
        val wrapped = ByteArray(wrappedLen)
        input.readFully(wrapped)
        return ExportedVaultCryptoState(salt, iv, wrapped)
    }

    private fun copyExact(input: DataInputStream, out: FileOutputStream, len: Long) {
        var remaining = len
        val buffer = ByteArray(8192)
        while (remaining > 0) {
            val toRead = min(buffer.size.toLong(), remaining).toInt()
            input.readFully(buffer, 0, toRead)
            out.write(buffer, 0, toRead)
            remaining -= toRead
        }
    }
}
