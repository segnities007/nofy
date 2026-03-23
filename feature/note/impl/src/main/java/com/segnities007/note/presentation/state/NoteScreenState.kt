package com.segnities007.note.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface

/** composition スコープで [NoteScreenState] を 1 インスタンス保持する。 */
@Composable
internal fun rememberNoteScreenState(): NoteScreenState {
    return remember { NoteScreenState() }
}

/**
 * エディタ操作と連動するクローム状態。VM の [com.segnities007.note.presentation.contract.NoteState] とは別軸。
 */
@Stable
internal class NoteScreenState {
    /** 上下フローティングバーを表示するか（エディタフォーカス等で隠す）。 */
    var areBarsVisible by mutableStateOf(true)
        private set

    /** 非 null のとき削除確認ダイアログを出す対象ページ ID。 */
    var pendingDeletePageId: String? by mutableStateOf(null)
        private set

    /** バーを再表示する（ページ移動やダイアログ表示前に呼ぶ）。 */
    fun showBars() {
        areBarsVisible = true
    }

    /** エディタからのコールバックでバー表示を直接更新する。 */
    fun updateBarsVisibility(visible: Boolean) {
        areBarsVisible = visible
    }

    /** 削除確認を開き、バーを表示した状態にする。 */
    fun requestDelete(pageId: String) {
        pendingDeletePageId = pageId
        showBars()
    }

    /** 削除をキャンセルしたときにダイアログを閉じる。 */
    fun dismissDeleteDialog() {
        pendingDeletePageId = null
    }
}

@NofyPreview
@Composable
private fun NoteScreenStatePreview() {
    val screenState = rememberNoteScreenState()

    NofyPreviewSurface {
        NofyText(
            text = "bars=${screenState.areBarsVisible}, delete=${screenState.pendingDeletePageId ?: "none"}"
        )
    }
}
