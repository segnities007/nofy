package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R
import kotlinx.coroutines.delay

@Composable
internal fun NoteEditorPage(
    content: String,
    onContentChange: (String) -> Unit,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val editorValue = remember { mutableStateOf(TextFieldValue(text = content)) }
    val isFocused = remember { mutableStateOf(false) }
    val cursorRect = remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(content) {
        if (content != editorValue.value.text) {
            val constrainedSelection = editorValue.value.selection.end.coerceIn(0, content.length)
            editorValue.value = editorValue.value.copy(
                text = content,
                selection = TextRange(constrainedSelection)
            )
        }
    }

    LaunchedEffect(isFocused.value) {
        if (!isFocused.value) return@LaunchedEffect
        delay(FocusBringIntoViewDelayMillis)
        val currentCursorRect = cursorRect.value ?: return@LaunchedEffect
        bringIntoViewRequester.bringIntoView(currentCursorRect)
    }

    LaunchedEffect(cursorRect.value, editorValue.value.selection, isFocused.value) {
        val currentCursorRect = cursorRect.value ?: return@LaunchedEffect
        if (!isFocused.value) return@LaunchedEffect
        bringIntoViewRequester.bringIntoView(currentCursorRect)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val editorInnerTopPadding = rememberEditorInnerTopPadding()
        val editorMinHeight = maxHeight
        val listState = rememberLazyListState()
        ObserveNoteBarsVisibilityOnScroll(
            listState = listState,
            onBarsVisibilityChange = onBarsVisibilityChange
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = editorMinHeight)
                ) {
                    BasicTextField(
                        value = editorValue.value,
                        onValueChange = { updatedValue: TextFieldValue ->
                            editorValue.value = updatedValue
                            onContentChange(updatedValue.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = editorMinHeight)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusChanged { focusState ->
                                isFocused.value = focusState.isFocused
                            },
                        textStyle = NofyThemeTokens.typography.bodyLarge.copy(
                            color = NofyThemeTokens.colorScheme.onSurface,
                            lineHeight = 28.sp
                        ),
                        cursorBrush = SolidColor(NofyThemeTokens.colorScheme.primary),
                        onTextLayout = { layoutResult ->
                            val cursorOffset = editorValue.value.selection.end.coerceIn(
                                minimumValue = 0,
                                maximumValue = editorValue.value.text.length
                            )
                            cursorRect.value = layoutResult.getCursorRect(cursorOffset)
                        },
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = editorInnerTopPadding)
                            ) {
                                if (editorValue.value.text.isBlank()) {
                                    NofyText(
                                        text = stringResource(R.string.note_placeholder),
                                        style = NofyThemeTokens.typography.bodyLarge,
                                        color = NofyThemeTokens.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
    }
}

private const val FocusBringIntoViewDelayMillis = 250L
private val EditorInnerContentPadding = 12.dp

@Composable
private fun rememberEditorInnerTopPadding(): Dp {
    return WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        NofyFloatingBarDefaults.TopBarOverlayHeight +
        EditorInnerContentPadding
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
