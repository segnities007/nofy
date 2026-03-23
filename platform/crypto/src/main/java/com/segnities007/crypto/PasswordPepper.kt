package com.segnities007.crypto

/**
 * パスワードハッシュに追加のペッパー処理を施す（Argon2 等のパラメータとともに適用）。
 */
interface PasswordPepper {
    /**
     * Argon2 等で得た [hash] に、追加のペッパー処理を施したバイト列を返す。
     *
     * @param hash ソルト付きハッシュの生バイト
     * @param salt 使用したソルト
     * @param tCost 時間コスト
     * @param mCost メモリコスト（実装依存の単位）
     * @param parallelism 並列度
     */
    fun pepper(
        hash: ByteArray,
        salt: ByteArray,
        tCost: Int,
        mCost: Int,
        parallelism: Int
    ): ByteArray
}
