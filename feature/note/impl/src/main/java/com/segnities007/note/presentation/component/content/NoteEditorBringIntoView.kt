package com.segnities007.note.presentation.component.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

internal const val NoteEditorFocusBringIntoViewDelayMillis = 250L

/**
 * 親 [content] とローカル [editorValue] の同期（選択範囲を保ちつつテキストだけ差し替え）。
 */
@Composable
internal fun NoteEditorSyncContentFromParent(
    content: String,
    fieldState: NoteEditorFieldStateHolder
) {
    LaunchedEffect(content) {
        if (content != fieldState.editorValue.text) {
            val constrainedSelection = fieldState.editorValue.selection.end.coerceIn(0, content.length)
            fieldState.editorValue = fieldState.editorValue.copy(
                text = content,
                selection = TextRange(constrainedSelection)
            )
        }
    }
}

/**
 * フォーカス直後とカーソル移動時に、IME 下でカーソルが隠れないよう [BringIntoViewRequester] で持ち上げる。
 */
@Composable
internal fun NoteEditorBringIntoViewEffects(
    fieldState: NoteEditorFieldStateHolder,
    topContentPadding: Dp,
    density: Density
) {
    LaunchedEffect(fieldState.isFocused, topContentPadding, density) {
        if (!fieldState.isFocused) return@LaunchedEffect
        delay(NoteEditorFocusBringIntoViewDelayMillis)
        val requestRect = fieldState.cursorRect?.toNoteEditorBringIntoViewRect(
            topContentPadding = topContentPadding,
            density = density
        ) ?: return@LaunchedEffect
        fieldState.bringIntoViewRequester.bringIntoView(requestRect)
    }

    LaunchedEffect(
        fieldState.cursorRect,
        fieldState.editorValue.selection,
        fieldState.isFocused,
        topContentPadding,
        density
    ) {
        if (!fieldState.isFocused) return@LaunchedEffect
        val requestRect = fieldState.cursorRect?.toNoteEditorBringIntoViewRect(
            topContentPadding = topContentPadding,
            density = density
        ) ?: return@LaunchedEffect
        fieldState.bringIntoViewRequester.bringIntoView(requestRect)
    }
}

internal fun Rect.toNoteEditorBringIntoViewRect(
    topContentPadding: Dp,
    density: Density
): Rect {
    val topOffset = with(density) { topContentPadding.toPx() }
    val bottomClearance = with(density) { NotePageBottomContentPadding.toPx() }
    return Rect(
        left = left,
        top = top + topOffset,
        right = right,
        bottom = bottom + topOffset + bottomClearance
    )
}
