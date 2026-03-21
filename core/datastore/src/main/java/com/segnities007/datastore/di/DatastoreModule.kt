package com.segnities007.datastore.di

import android.content.Context
import com.segnities007.datastore.AuthLocalDataSource
import com.segnities007.datastore.KeystorePreferencesStore
import com.segnities007.datastore.SettingsLocalDataSource
import com.segnities007.datastore.SecureUiSettingsRepository
import com.segnities007.settings.UiSettingsRepository
import org.koin.dsl.module

val datastoreModule = module {
    single { KeystorePreferencesStore(get<Context>()) }
    single { AuthLocalDataSource(get()) }
    single { SettingsLocalDataSource(get()) }
    single<UiSettingsRepository> { SecureUiSettingsRepository(get()) }
}
