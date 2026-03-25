package com.segnities007.login.domain.model

/** ログイン／設定で参照する利用者スナップショット（現状は主に生体フラグ用）。 */
data class User(
    val id: String,
    val username: String,
    val isBiometricEnabled: Boolean
)
