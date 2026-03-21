package com.segnities007.nofy.di

import com.segnities007.auth.di.authModule
import com.segnities007.biometric.di.biometricModule
import com.segnities007.crypto.di.cryptoModule
import com.segnities007.datastore.di.datastoreModule
import com.segnities007.login.di.loginFeatureModule
import com.segnities007.note.di.noteFeatureModule
import com.segnities007.nofy.security.RiskyEnvironmentDetector
import com.segnities007.setting.di.settingFeatureModule
import org.koin.core.module.Module
import org.koin.dsl.module

val nofyModules: List<Module> = listOf(
    biometricModule,
    cryptoModule,
    datastoreModule,
    authModule,
    loginFeatureModule,
    noteFeatureModule,
    settingFeatureModule,
    module {
        single { RiskyEnvironmentDetector(get()) }
    }
)
