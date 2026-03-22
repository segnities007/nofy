package com.segnities007.crypto.di

import com.segnities007.crypto.BiometricCipher
import com.segnities007.crypto.DataCipher
import com.segnities007.crypto.KeystorePasswordPepper
import com.segnities007.crypto.PasswordHasher
import com.segnities007.crypto.PasswordPepper
import org.koin.dsl.module

val cryptoModule = module {
    single<PasswordPepper> { KeystorePasswordPepper() }
    single { PasswordHasher(get()) }
    single { DataCipher(get()) }
    single { BiometricCipher() }
}
