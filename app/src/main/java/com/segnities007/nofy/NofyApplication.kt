package com.segnities007.nofy

import android.app.Application
import com.segnities007.nofy.di.nofyModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NofyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@NofyApplication)
            modules(nofyModules)
        }
    }
}
