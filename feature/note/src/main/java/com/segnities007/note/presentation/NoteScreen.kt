package com.segnities007.note.presentation

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.molecule.dialog.NofyConfirmationDialog
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.note.R
import com.segnities007.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

@Composable
fun NoteScreen(
    noteRepository: NoteRepository,
    authRepository: AuthRepository,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: NoteViewModel = viewModel(
        factory = noteViewModelFactory(
            noteRepository = noteRepository,
            authRepository = authRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = uiState.currentPageIndex,
        pageCount = { uiState.pages.size }
    )
    val screenState = rememberNoteScreenState()

    ObserveNoteEffects(
        effect = viewModel.effect,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToLogin = onNavigateToLogin
    )
    SyncPagerWithState(
        pagerState = pagerState,
        currentPageIndex = uiState.currentPageIndex,
        pageCount = uiState.pages.size
    )
    SyncStateWithPager(
        pagerState = pagerState,
        currentPageIndex = uiState.currentPageIndex,
        pageCount = uiState.pages.size,
        onPageChanged = { index -> viewModel.onIntent(NoteIntent.PageChanged(index)) },
        onPagerSettled = screenState::showBars
    )

    NoteScreenContent(
        uiState = uiState,
        pagerState = pagerState,
        screenState = screenState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
private fun NoteScreenContent(
    uiState: NoteState,
    pagerState: PagerState,
    screenState: NoteScreenState,
    onIntent: (NoteIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val chromeState = uiState.toChromeState(
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

            AnimatedVisibility(
                visible = chromeState.canShowBars && screenState.areBarsVisible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
            ) {
                NoteTopBar(
                    title = chromeState.title,
                    canDelete = chromeState.canDelete,
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
                visible = chromeState.canShowBars && screenState.areBarsVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                NoteBottomBar(
                    isPreviewEnabled = uiState.isPreviewEnabled,
                    currentPage = chromeState.currentPage,
                    totalPages = chromeState.totalPages,
                    canNavigatePrevious = chromeState.canNavigatePrevious,
                    canNavigateNext = chromeState.canNavigateNext,
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

@Composable
private fun ObserveNoteEffects(
    effect: Flow<NoteEffect>,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(effect, context) {
        effect.collect { handledEffect ->
            when (handledEffect) {
                is NoteEffect.ShowToastRes -> showToast(context, handledEffect.messageRes)
                NoteEffect.NavigateToSettings -> onNavigateToSettings()
                NoteEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }
}

@Composable
private fun SyncPagerWithState(
    pagerState: PagerState,
    currentPageIndex: Int,
    pageCount: Int
) {
    LaunchedEffect(currentPageIndex, pageCount) {
        val targetPage = currentPageIndex.coerceIn(0, pageCount - 1)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }
}

@Composable
private fun SyncStateWithPager(
    pagerState: PagerState,
    currentPageIndex: Int,
    pageCount: Int,
    onPageChanged: (Int) -> Unit,
    onPagerSettled: () -> Unit
) {
    LaunchedEffect(pagerState.currentPage, pageCount) {
        val safePage = pagerState.currentPage.coerceIn(0, pageCount - 1)
        if (safePage != currentPageIndex) {
            onPageChanged(safePage)
        }
        onPagerSettled()
    }
}

@Composable
private fun NoteDeleteDialog(
    page: NotePageUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (page.title.isBlank()) {
        stringResource(R.string.note_delete_dialog_message)
    } else {
        stringResource(
            R.string.note_delete_dialog_message_named,
            page.title
        )
    }

    NofyConfirmationDialog(
        title = stringResource(R.string.note_delete_dialog_title),
        message = message,
        confirmLabel = stringResource(R.string.note_delete_action),
        dismissLabel = stringResource(R.string.note_cancel_action),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

private fun showToast(
    context: Context,
    @StringRes messageRes: Int
) {
    Toast.makeText(
        context,
        context.getString(messageRes),
        Toast.LENGTH_SHORT
    ).show()
}

@Preview(showBackground = true)
@Composable
private fun NoteScreenPreview() {
    NofyTheme {
        NofySurface {
            Box(modifier = Modifier.fillMaxSize()) {
                NoteBody(
                    uiState = NoteState(
                        pages = listOf(
                            NotePageUiState(
                                pageId = "preview",
                                noteId = 1L,
                                content = "# Secure note\n- First item\n- Second item"
                            ),
                            NotePageUiState.blank(pageId = "blank")
                        ),
                        isLoading = false
                    ),
                    pagerState = rememberPagerState(
                        initialPage = 0,
                        pageCount = { 2 }
                    ),
                    onEditContent = { _, _ -> },
                    onRetry = {},
                    onBarsVisibilityChange = {}
                )

                NoteTopBar(
                    title = "Secure note",
                    canDelete = true,
                    onDelete = {},
                    onLock = {}
                )

                NoteBottomBar(
                    isPreviewEnabled = false,
                    currentPage = 0,
                    totalPages = 2,
                    canNavigatePrevious = false,
                    canNavigateNext = true,
                    onPrevious = {},
                    onNext = {},
                    onTogglePreview = {},
                    onSettings = {}
                )
            }
        }
    }
}
