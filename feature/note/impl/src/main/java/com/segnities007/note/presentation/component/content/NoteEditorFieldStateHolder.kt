package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.input.TextFieldValue

/**
 * エディタの [TextFieldValue]・フォーカス・カーソル矩形・bringIntoView をまとめた UI 状態。
 * [NoteEditorPage] の effect / レイアウトで共有する。
 */
internal class NoteEditorFieldStateHolder(
    val bringIntoViewRequester: BringIntoViewRequester,
    initialContent: String,
) {
    var editorValue by mutableStateOf(TextFieldValue(text = initialContent))
    var isFocused by mutableStateOf(false)
    var cursorRect by mutableStateOf<Rect?>(null)
}

@Composable
internal fun rememberNoteEditorFieldState(initialContent: String): NoteEditorFieldStateHolder {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    return remember {
        NoteEditorFieldStateHolder(
            bringIntoViewRequester = bringIntoViewRequester,
            initialContent = initialContent
        )
    }
}
