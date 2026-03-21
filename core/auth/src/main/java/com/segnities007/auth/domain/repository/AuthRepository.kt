package com.segnities007.auth.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * アプリのロックと認証状態を管理するリポジトリ。
 * coreモジュールとして公開され、各featureからAPIとして叩かれる。
 */
interface AuthRepository {
    fun isRegistered(): Flow<Boolean>
    fun isBiometricEnabled(): Flow<Boolean>
    fun isLocked(): Flow<Boolean>
    suspend fun lock(): Result<Unit>
    suspend fun unlock(password: String): Result<Unit>
    suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit>
    suspend fun registerPassword(password: String): Result<Unit>
    suspend fun saveBiometricSecret(encryptedSecret: ByteArray, iv: ByteArray): Result<Unit>
    suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>?
    suspend fun clearBiometricSecret(): Result<Unit>
    suspend fun reset(currentPassword: String): Result<Unit>
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}
