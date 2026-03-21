package com.segnities007.login.di

import com.segnities007.biometric.BiometricAuthenticator
import com.segnities007.login.domain.usecase.ClearBiometricSecretUseCase
import com.segnities007.login.domain.usecase.ObserveBiometricEnabledUseCase
import com.segnities007.login.domain.usecase.RegisterPasswordUseCase
import com.segnities007.login.domain.usecase.SaveBiometricSecretUseCase
import com.segnities007.login.domain.usecase.UnlockWithBiometricUseCase
import com.segnities007.login.domain.usecase.UnlockWithPasswordUseCase
import com.segnities007.login.presentation.biometric.LoginBiometricHandler
import com.segnities007.login.presentation.navigation.LoginNavigationEntryInstaller
import com.segnities007.login.presentation.viewmodel.LoginViewModel
import com.segnities007.login.presentation.viewmodel.RegisterViewModel
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.login.presentation.operation.AuthenticateWithCryptoOperation
import com.segnities007.login.presentation.operation.DecryptBiometricPasswordOperation
import com.segnities007.login.presentation.operation.EncryptBiometricSecretOperation
import com.segnities007.login.presentation.operation.PrepareBiometricEnrollmentOperation
import com.segnities007.login.presentation.operation.PrepareBiometricUnlockOperation
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val loginFeatureModule = module {
    singleOf(::LoginNavigationEntryInstaller) bind NavigationEntryInstaller::class
    factory { UnlockWithPasswordUseCase(get()) }
    factory { UnlockWithBiometricUseCase(get()) }
    factory { ObserveBiometricEnabledUseCase(get()) }
    factory { ClearBiometricSecretUseCase(get()) }
    factory { RegisterPasswordUseCase(get()) }
    factory { SaveBiometricSecretUseCase(get()) }
    factory { PrepareBiometricUnlockOperation(get(), get()) }
    factory { DecryptBiometricPasswordOperation(get()) }
    factory { PrepareBiometricEnrollmentOperation(get()) }
    factory { EncryptBiometricSecretOperation(get()) }
    viewModel { (biometricHandler: LoginBiometricHandler) ->
        LoginViewModel(
            unlockWithPasswordUseCase = get(),
            observeBiometricEnabledUseCase = get(),
            clearBiometricSecretUseCase = get(),
            prepareBiometricUnlockOperation = get(),
            authenticateWithCryptoOperation = AuthenticateWithCryptoOperation(biometricHandler),
            decryptBiometricPasswordOperation = get(),
            unlockWithBiometricUseCase = get()
        )
    }
    viewModel { (biometricHandler: LoginBiometricHandler) ->
        RegisterViewModel(
            registerPasswordUseCase = get(),
            prepareBiometricEnrollmentOperation = get(),
            authenticateWithCryptoOperation = AuthenticateWithCryptoOperation(biometricHandler),
            encryptBiometricSecretOperation = get(),
            saveBiometricSecretUseCase = get()
        )
    }
}
