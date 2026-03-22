package com.segnities007.nofy

import android.app.Application
import com.segnities007.nofy.di.nofyModules
import com.segnities007.nofy.security.RiskyEnvironmentDetector
import com.segnities007.nofy.security.RiskyEnvironmentSnapshotHolder
import org.koin.android.ext.android.getKoin
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

        syncRiskyEnvironmentSnapshot()
    }

    private fun syncRiskyEnvironmentSnapshot() {
        with(getKoin()) {
            get<RiskyEnvironmentSnapshotHolder>().publish(get<RiskyEnvironmentDetector>().detect())
        }
    }
}
