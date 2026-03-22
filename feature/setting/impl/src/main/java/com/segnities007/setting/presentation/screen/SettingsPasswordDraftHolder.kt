package com.segnities007.setting.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.segnities007.setting.presentation.contract.PasswordChangeDraft

/**
 * 平文パスワード下書きを **ViewModel の StateFlow に載せない**ための composition 専用ホルダー。
 * 詳細は [PasswordChangeDraft] の KDoc を参照。
 */
internal interface SettingsPasswordDraftHolder {
    val passwordDraft: PasswordChangeDraft
    fun setPasswordDraft(value: PasswordChangeDraft)
    fun clearPasswordDraft()
}

@Composable
internal fun rememberSettingsPasswordDraftHolder(): SettingsPasswordDraftHolder {
    return remember { SettingsPasswordDraftHolderImpl() }
}

private class SettingsPasswordDraftHolderImpl : SettingsPasswordDraftHolder {
    private var backing by mutableStateOf(PasswordChangeDraft())

    override val passwordDraft: PasswordChangeDraft
        get() = backing

    override fun setPasswordDraft(value: PasswordChangeDraft) {
        backing = value
    }

    override fun clearPasswordDraft() {
        backing = PasswordChangeDraft()
    }
}
