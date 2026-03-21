package com.segnities007.note.presentation.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.note.R
import com.segnities007.note.presentation.component.bar.NoteBottomBar
import com.segnities007.note.presentation.component.bar.NoteTopBar
import com.segnities007.note.presentation.component.content.NoteBody
import com.segnities007.note.presentation.component.dialog.NoteDeleteDialog
import com.segnities007.note.presentation.contract.NoteIntent
import com.segnities007.note.presentation.contract.NoteState
import com.segnities007.note.presentation.preview.previewNotePages
import com.segnities007.note.presentation.preview.previewNoteState
import com.segnities007.note.presentation.state.NoteScreenState
import com.segnities007.note.presentation.state.findPage
import com.segnities007.note.presentation.state.toBarState

@Composable
internal fun NoteScaffold(
    uiState: NoteState,
    pagerState: PagerState,
    screenState: NoteScreenState,
    onIntent: (NoteIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val barState = uiState.toBarState(
        untitledTitle = stringResource(R.string.note_title_untitled)
    )
    val pendingDeletePage = uiState.pages.findPage(screenState.pendingDeletePageId)

    NofySurface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            NoteBody(
                uiState = uiState,
                pagerState = pagerState,
                onEditContent = { pageId, content ->
                    onIntent(NoteIntent.EditContent(pageId, content))
                },
                onRetry = { onIntent(NoteIntent.Reload) },
                onBarsVisibilityChange = screenState::updateBarsVisibility
            )

            NoteEdgeScrim(
                modifier = Modifier.align(Alignment.TopCenter),
                height = NofyFloatingBarDefaults.TopBarOverlayHeight,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.32f),
                        Color.Transparent
                    )
                )
            )

            NoteEdgeScrim(
                modifier = Modifier.align(Alignment.BottomCenter),
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.32f)
                    )
                )
            )

            AnimatedVisibility(
                visible = barState.canShowBars && screenState.areBarsVisible,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
            ) {
                NoteTopBar(
                    modifier = Modifier,
                    title = barState.title,
                    canDelete = barState.canDelete,
                    onDelete = {
                        uiState.currentPage?.let { currentPage ->
                            screenState.requestDelete(currentPage.pageId)
                        }
                    },
                    onLock = {
                        screenState.showBars()
                        onIntent(NoteIntent.Lock)
                    }
                )
            }

            AnimatedVisibility(
                visible = barState.canShowBars && screenState.areBarsVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                NoteBottomBar(
                    isPreviewEnabled = uiState.isPreviewEnabled,
                    currentPage = barState.currentPage,
                    totalPages = barState.totalPages,
                    canNavigatePrevious = barState.canNavigatePrevious,
                    canNavigateNext = barState.canNavigateNext,
                    onPrevious = {
                        screenState.showBars()
                        onIntent(NoteIntent.NavigateToPreviousPage)
                    },
                    onNext = {
                        screenState.showBars()
                        onIntent(NoteIntent.NavigateToNextPage)
                    },
                    onTogglePreview = {
                        screenState.showBars()
                        onIntent(NoteIntent.TogglePreview)
                    },
                    onSettings = {
                        screenState.showBars()
                        onIntent(NoteIntent.NavigateToSettings)
                    }
                )
            }

            pendingDeletePage?.let { currentPage ->
                NoteDeleteDialog(
                    page = currentPage,
                    onConfirm = {
                        screenState.dismissDeleteDialog()
                        onIntent(NoteIntent.DeletePage(currentPage.pageId))
                    },
                    onDismiss = screenState::dismissDeleteDialog
                )
            }
        }
    }
}

@NofyPreview
@Composable
private fun NoteScaffoldPreview() {
    val state = previewNoteState()
    val screenState = remember {
        NoteScreenState().apply {
            requestDelete(previewNotePages().first().pageId)
        }
    }

    NoteScaffold(
        uiState = state,
        pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { state.pages.size }
        ),
        screenState = screenState,
        onIntent = {}
    )
}

@Composable
private fun NoteEdgeScrim(
    brush: Brush,
    height: Dp = 144.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush)
    )
}
