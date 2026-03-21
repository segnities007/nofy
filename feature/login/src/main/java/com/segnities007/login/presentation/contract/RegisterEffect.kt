package com.segnities007.login.presentation.contract

import androidx.annotation.StringRes

sealed interface RegisterEffect {
    data class ShowToastRes(@param:StringRes val messageRes: Int) : RegisterEffect
    data class NavigateToLogin(@param:StringRes val messageRes: Int? = null) : RegisterEffect
}
