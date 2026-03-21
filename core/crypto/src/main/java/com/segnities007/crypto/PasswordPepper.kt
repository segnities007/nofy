package com.segnities007.crypto

interface PasswordPepper {
    fun pepper(
        hash: ByteArray,
        salt: ByteArray,
        tCost: Int,
        mCost: Int,
        parallelism: Int
    ): ByteArray
}
