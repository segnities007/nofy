package com.segnities007.login.domain.model

data class User(
    val id: String,
    val username: String,
    val isBiometricEnabled: Boolean
)
