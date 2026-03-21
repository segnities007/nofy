package com.segnities007.database

interface SecureDatabaseController {
    fun unlock(passphrase: ByteArray)

    fun changePassphrase(currentPassphrase: String, newPassphrase: String)

    fun lock()

    fun deleteDatabaseFiles()
}
