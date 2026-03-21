package com.segnities007.database

interface SecureDatabaseController {
    fun unlock(passphrase: ByteArray)

    fun lock()

    fun deleteDatabaseFiles()
}
