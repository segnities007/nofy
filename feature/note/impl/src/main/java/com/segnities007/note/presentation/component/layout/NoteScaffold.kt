package com.segnities007.note.presentation.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.surface.NofyFullscreenSurface
import com.segnities007.designsystem.template.NofyBrushedFloatingBarScreen
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
    val barsVisible = barState.canShowBars && screenState.areBarsVisible

    NofyFullscreenSurface(modifier = modifier) {
        NofyBrushedFloatingBarScreen(
            modifier = Modifier.fillMaxSize(),
            showEdgeBrushes = true,
            body = {
                NoteBody(
                    uiState = uiState,
                    pagerState = pagerState,
                    onEditContent = { pageId, content ->
                        onIntent(NoteIntent.EditContent(pageId, content))
                    },
                    onRetry = { onIntent(NoteIntent.Reload) },
                    onBarsVisibilityChange = screenState::updateBarsVisibility
                )
            },
            header = { topModifier ->
                NoteFloatingBarSlideVisibility(
                    visible = barsVisible,
                    slideFromTop = true,
                    modifier = topModifier
                ) {
                    NoteTopBar(
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
            },
            footer = { bottomModifier ->
                NoteFloatingBarSlideVisibility(
                    visible = barsVisible,
                    slideFromTop = false,
                    modifier = bottomModifier
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
            },
            overlay = {
                pendingDeletePage?.let { page ->
                    NoteDeleteDialog(
                        page = page,
                        onConfirm = {
                            screenState.dismissDeleteDialog()
                            onIntent(NoteIntent.DeletePage(page.pageId))
                        },
                        onDismiss = screenState::dismissDeleteDialog
                    )
                }
            }
        )
    }
}

@Composable
private fun NoteFloatingBarSlideVisibility(
    visible: Boolean,
    slideFromTop: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val enterSlide = if (slideFromTop) {
        slideInVertically(initialOffsetY = { -it / 2 })
    } else {
        slideInVertically(initialOffsetY = { it / 2 })
    }
    val exitSlide = if (slideFromTop) {
        slideOutVertically(targetOffsetY = { -it })
    } else {
        slideOutVertically(targetOffsetY = { it })
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + enterSlide,
        exit = fadeOut() + exitSlide,
    ) {
        content()
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
