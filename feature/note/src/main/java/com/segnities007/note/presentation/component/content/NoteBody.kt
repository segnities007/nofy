package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.indicator.NofyLoadingIndicator
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R
import com.segnities007.note.presentation.contract.NoteState
import com.segnities007.note.presentation.preview.previewNoteState

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
                key = { pageIndex ->
                    uiState.pages.getOrNull(pageIndex)?.pageId ?: "missing-page-$pageIndex"
                },
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val page = uiState.pages.getOrNull(pageIndex)
                if (page != null) {
                    NotePagerPage(
                        page = page,
                        isPreviewEnabled = uiState.isPreviewEnabled,
                        onContentChange = { updatedContent ->
                            onEditContent(page.pageId, updatedContent)
                        },
                        onBarsVisibilityChange = onBarsVisibilityChange
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@NofyPreview
@Composable
private fun NoteBodyPreview() {
    val state = previewNoteState()

    NofyPreviewSurface {
        NoteBody(
            uiState = state,
            pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { state.pages.size }
            ),
            onEditContent = { _, _ -> },
            onRetry = {},
            onBarsVisibilityChange = {}
        )
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
