package com.segnities007.auth.di

import com.segnities007.auth.data.repository.AuthRepositoryImpl
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.auth.domain.usecase.LockApplicationUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** [AuthRepository] の単一実装登録と共有ユースケース。 */
val authModule = module {
    factoryOf(::LockApplicationUseCase)
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
}
