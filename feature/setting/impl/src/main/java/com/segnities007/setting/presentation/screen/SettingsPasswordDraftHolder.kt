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
    /** パスワード変更フォームの現在の下書き（平文は StateFlow に載せない）。 */
    val passwordDraft: PasswordChangeDraft

    /** 下書き全体を置き換える。 */
    fun setPasswordDraft(value: PasswordChangeDraft)

    /** 下書きを空に戻す。 */
    fun clearPasswordDraft()
}

/** composition スコープで [SettingsPasswordDraftHolder] を保持する。 */
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
