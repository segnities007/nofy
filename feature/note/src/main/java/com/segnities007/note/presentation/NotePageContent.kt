package com.segnities007.note.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.indicator.NofyLoadingIndicator
import com.segnities007.designsystem.atom.markdown.NofyMarkdown
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

@Composable
internal fun NoteBody(
    uiState: NoteState,
    pagerState: PagerState,
    onEditContent: (String, String) -> Unit,
    onRetry: () -> Unit,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    when {
        uiState.isLoading && uiState.pages.all { it.isBlank } -> {
            NoteLoadingState()
        }

        uiState.error != null && uiState.pages.all { it.isBlank } -> {
            NoteLoadErrorState(onRetry = onRetry)
        }

        else -> {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { pageIndex -> uiState.pages[pageIndex].pageId },
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val page = uiState.pages[pageIndex]
                NotePage(
                    page = page,
                    isPreviewEnabled = uiState.isPreviewEnabled,
                    onContentChange = { updatedContent ->
                        onEditContent(page.pageId, updatedContent)
                    },
                    onBarsVisibilityChange = onBarsVisibilityChange
                )
            }
        }
    }
}

@Composable
private fun NoteLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        NofyLoadingIndicator()
    }
}

@Composable
private fun NoteLoadErrorState(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        NofyCardSurface {
            NofyText(
                text = stringResource(R.string.note_error_title),
                style = NofyThemeTokens.typography.titleLarge
            )
            NofyText(
                text = stringResource(R.string.note_error_body),
                style = NofyThemeTokens.typography.bodyMedium,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
            NofyButton(
                text = stringResource(R.string.note_retry_action),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NotePage(
    page: NotePageUiState,
    isPreviewEnabled: Boolean,
    onContentChange: (String) -> Unit,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NofyThemeTokens.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    top = NofyFloatingBarDefaults.TopBarReservedSpace,
                    end = 20.dp,
                    bottom = NofyFloatingBarDefaults.BottomBarReservedSpace + 4.dp
                )
        ) {
            if (isPreviewEnabled) {
                NotePreviewContent(
                    content = page.content,
                    onBarsVisibilityChange = onBarsVisibilityChange
                )
            } else {
                NoteEditorContent(
                    content = page.content,
                    onContentChange = onContentChange,
                    onBarsVisibilityChange = onBarsVisibilityChange
                )
            }
        }
    }
}

@Composable
private fun NoteEditorContent(
    content: String,
    onContentChange: (String) -> Unit,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val listState = rememberLazyListState()
        ObserveScrollDirection(
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
                        .heightIn(min = maxHeight)
                ) {
                    if (content.isBlank()) {
                        NofyText(
                            text = stringResource(R.string.note_placeholder),
                            style = NofyThemeTokens.typography.bodyLarge,
                            color = NofyThemeTokens.colorScheme.onSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = content,
                        onValueChange = onContentChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = NofyThemeTokens.typography.bodyLarge.copy(
                            color = NofyThemeTokens.colorScheme.onSurface,
                            lineHeight = 28.sp
                        ),
                        cursorBrush = SolidColor(NofyThemeTokens.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotePreviewContent(
    content: String,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    ObserveScrollDirection(
        listState = listState,
        onBarsVisibilityChange = onBarsVisibilityChange
    )

    if (content.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            NofyText(
                text = stringResource(R.string.note_preview_empty),
                style = NofyThemeTokens.typography.bodyLarge,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            NofyMarkdown(
                content = content,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ObserveScrollDirection(
    listState: LazyListState,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) ->
                val direction = when {
                    index != previousIndex -> {
                        if (index > previousIndex) 1 else -1
                    }

                    abs(offset - previousOffset) > 6 -> {
                        if (offset > previousOffset) 1 else -1
                    }

                    else -> 0
                }

                if (direction > 0) {
                    onBarsVisibilityChange(false)
                } else if (direction < 0) {
                    onBarsVisibilityChange(true)
                }

                previousIndex = index
                previousOffset = offset
            }
    }
}
