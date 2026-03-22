package com.segnities007.auth.di

import com.segnities007.auth.data.repository.AuthRepositoryImpl
import com.segnities007.auth.domain.repository.AuthRepository
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
}
