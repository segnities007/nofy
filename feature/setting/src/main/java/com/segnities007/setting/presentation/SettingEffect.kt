package com.segnities007.setting.presentation

import androidx.annotation.StringRes

sealed interface SettingEffect {
    data class ShowToastRes(@param:StringRes val messageRes: Int) : SettingEffect
    data object NavigateToLogin : SettingEffect
}
