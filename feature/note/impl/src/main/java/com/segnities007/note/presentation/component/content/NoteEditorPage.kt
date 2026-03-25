package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.theme.NofyEditorTypography
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R

@Composable
internal fun NoteEditorPage(
    content: String,
    onContentChange: (String) -> Unit,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    val fieldState = rememberNoteEditorFieldState(initialContent = content)
    val topContentPadding = NoteUnderFloatingBars.topContentPadding()
    val density = LocalDensity.current

    NoteEditorSyncContentFromParent(content = content, fieldState = fieldState)
    NoteEditorBringIntoViewEffects(
        fieldState = fieldState,
        topContentPadding = topContentPadding,
        density = density
    )

    NoteEditorScrollScaffold(
        fieldState = fieldState,
        topContentPadding = topContentPadding,
        onContentChange = onContentChange,
        onBarsVisibilityChange = onBarsVisibilityChange
    )
}

@Composable
private fun NoteEditorScrollScaffold(
    fieldState: NoteEditorFieldStateHolder,
    topContentPadding: Dp,
    onContentChange: (String) -> Unit,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val editorMinHeight = maxHeight
        val listState = rememberLazyListState()

        ObserveNoteBarsVisibilityOnScroll(
            listState = listState,
            onBarsVisibilityChange = onBarsVisibilityChange
        )

        NoteUnderFloatingBars.LazyMainColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            NoteEditorBodyField(
                fieldState = fieldState,
                topContentPadding = topContentPadding,
                minHeight = editorMinHeight,
                onContentChange = onContentChange
            )
        }
    }
}

@Composable
private fun NoteEditorBodyField(
    fieldState: NoteEditorFieldStateHolder,
    topContentPadding: Dp,
    minHeight: Dp,
    onContentChange: (String) -> Unit
) {
    val editorContentDescription = stringResource(R.string.note_a11y_note_editor)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        BasicTextField(
            value = fieldState.editorValue,
            onValueChange = { updated ->
                fieldState.editorValue = updated
                onContentChange(updated.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .semantics { contentDescription = editorContentDescription }
                .bringIntoViewRequester(fieldState.bringIntoViewRequester)
                .onFocusChanged { focusState ->
                    fieldState.isFocused = focusState.isFocused
                },
            textStyle = NofyEditorTypography.bodyStyle(),
            cursorBrush = SolidColor(NofyThemeTokens.colorScheme.primary),
            onTextLayout = { layoutResult ->
                val cursorOffset = fieldState.editorValue.selection.end.coerceIn(
                    minimumValue = 0,
                    maximumValue = fieldState.editorValue.text.length
                )
                fieldState.cursorRect = layoutResult.getCursorRect(cursorOffset)
            },
            decorationBox = { innerTextField ->
                NoteEditorDecorationBox(
                    showPlaceholder = fieldState.editorValue.text.isBlank(),
                    topContentPadding = topContentPadding,
                    innerTextField = innerTextField
                )
            }
        )
    }
}

@Composable
private fun NoteEditorDecorationBox(
    showPlaceholder: Boolean,
    topContentPadding: Dp,
    innerTextField: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topContentPadding)
    ) {
        if (showPlaceholder) {
            NofySupportingText(
                text = stringResource(R.string.note_placeholder),
                style = NofyEditorTypography.bodyStyle(
                    color = NofyThemeTokens.colorScheme.onSurfaceVariant,
                ),
            )
        }
        innerTextField()
    }
}

@NofyPreview
@Composable
private fun NoteEditorPagePreview() {
    NofyPreviewSurface {
        NoteEditorPage(
            content = "# Secure note\nWrite here...",
            onContentChange = {},
            onBarsVisibilityChange = {}
        )
    }
}
