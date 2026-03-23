package com.segnities007.auth.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * アプリのロックと認証状態を管理するリポジトリ。
 * coreモジュールとして公開され、各featureからAPIとして叩かれる。
 */
interface AuthRepository {
    /** 初回登録（マスターパスワード設定）が完了しているか。 */
    fun isRegistered(): Flow<Boolean>

    /** ログイン用の生体シークレットが保存され有効か。 */
    fun isBiometricEnabled(): Flow<Boolean>

    /** アプリがロックされ未解除か。 */
    fun isLocked(): Flow<Boolean>

    /** 入力パスワードが現在のマスターと一致するか検証する（ロック解除はしない）。 */
    suspend fun verifyPassword(password: String): Result<Unit>

    /** セッションを閉じ、アプリをロック状態にする。 */
    suspend fun lock(): Result<Unit>

    /** マスターパスワードで DB／セッションを開きロックを解除する。 */
    suspend fun unlock(password: String): Result<Unit>

    /** 生体で復号した平文パスワードでロックを解除する。 */
    suspend fun unlockWithBiometric(decryptedPassword: String): Result<Unit>

    /** 初回のマスターパスワードを登録する。 */
    suspend fun registerPassword(password: String): Result<Unit>

    /** 生体ログイン用に暗号化済みシークレットと IV を保存する。 */
    suspend fun saveBiometricSecret(encryptedSecret: ByteArray, iv: ByteArray): Result<Unit>

    /** 保存済みの生体用シークレットを取得する。未設定なら `null`。 */
    suspend fun getBiometricSecret(): Pair<ByteArray, ByteArray>?

    /** 生体用シークレットを削除し、生体ログインを無効化する。 */
    suspend fun clearBiometricSecret(): Result<Unit>

    /** マスターパスワード確認のうえ、認証状態と永続データを初期化する。 */
    suspend fun reset(currentPassword: String): Result<Unit>

    /** 生体ログインの利用可否フラグを更新する（シークレットの有無と整合させること）。 */
    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>

    /** 現在のマスターを検証したうえで新パスワードへ変更する。 */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /**
     * 移行済みボルト（DB とフィールド暗号の永続状態）が配置済みである前提で、
     * 引数のパスワードで DB とセッションを開き、この端末用の検証用ハッシュを保存する。
     */
    suspend fun adoptImportedVault(password: String): Result<Unit>
}
