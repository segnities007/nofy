package com.segnities007.biometric.di

import androidx.fragment.app.FragmentActivity
import com.segnities007.biometric.BiometricAuthenticator
import org.koin.dsl.module

/** Activity スコープの [BiometricAuthenticator] を factory で供給する。 */
val biometricModule = module {
    factory { (activity: FragmentActivity) -> BiometricAuthenticator(activity) }
}
