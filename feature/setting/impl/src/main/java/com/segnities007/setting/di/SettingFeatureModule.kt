package com.segnities007.setting.di

import android.app.Application
import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.setting.presentation.navigation.SettingNavigationEntryInstaller
import com.segnities007.setting.presentation.viewmodel.SettingViewModel
import com.segnities007.setting.presentation.viewmodel.VaultTransferReceiveViewModel
import com.segnities007.setting.presentation.viewmodel.VaultTransferSendViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** 設定とボルト送受信の ViewModel・ナビインストーラ。 */
val settingFeatureModule = module {
    singleOf(::SettingNavigationEntryInstaller) bind NavigationEntryInstaller::class
    viewModel { SettingViewModel(get(), get()) }
    viewModel {
        VaultTransferReceiveViewModel(
            androidContext().applicationContext as Application,
            get()
        )
    }
    viewModel {
        VaultTransferSendViewModel(
            androidContext().applicationContext as Application,
            get()
        )
    }
}
