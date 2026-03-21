package com.segnities007.note.presentation.contract

import androidx.annotation.StringRes

sealed interface NoteEffect {
    data class ShowToastRes(@param:StringRes val messageRes: Int) : NoteEffect
    data object NavigateToSettings : NoteEffect
    data object NavigateToLogin : NoteEffect
}
