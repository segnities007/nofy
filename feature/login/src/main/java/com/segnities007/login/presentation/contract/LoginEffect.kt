package com.segnities007.login.presentation.contract

import androidx.annotation.StringRes

sealed interface LoginEffect {
    data class ShowToastMessage(val message: String) : LoginEffect
    data class ShowToastRes(@param:StringRes val messageRes: Int) : LoginEffect
    data class ShowLockout(val remainingMillis: Long) : LoginEffect
    data object NavigateToNotes : LoginEffect
}
