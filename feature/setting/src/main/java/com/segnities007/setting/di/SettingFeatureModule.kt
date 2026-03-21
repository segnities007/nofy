package com.segnities007.setting.di

import com.segnities007.navigation.NavigationEntryInstaller
import com.segnities007.setting.presentation.navigation.SettingNavigationEntryInstaller
import com.segnities007.setting.presentation.viewmodel.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingFeatureModule = module {
    singleOf(::SettingNavigationEntryInstaller) bind NavigationEntryInstaller::class
    viewModel { SettingViewModel(get(), get()) }
}
