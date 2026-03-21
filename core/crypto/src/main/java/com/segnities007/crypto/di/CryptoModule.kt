package com.segnities007.crypto.di

import com.segnities007.crypto.BiometricCipher
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.PasswordHasher
import org.koin.dsl.module

val cryptoModule = module {
    single { PasswordHasher() }
    single { DataCipher() }
    single { BiometricCipher() }
}
