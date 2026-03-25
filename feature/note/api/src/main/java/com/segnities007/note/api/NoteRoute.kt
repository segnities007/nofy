package com.segnities007.note.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * ノート（メイン）画面のナビゲーションキー。
 */
@Serializable
sealed interface NoteRoute : NavKey {
    @Serializable
    data object NoteList : NoteRoute
}
