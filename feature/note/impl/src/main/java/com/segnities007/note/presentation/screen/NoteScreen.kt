package com.segnities007.note.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.util.showShortToast
import com.segnities007.note.presentation.component.layout.NoteScaffold
import com.segnities007.note.presentation.contract.NoteIntent
import com.segnities007.note.presentation.contract.NoteNavigationRequest
import com.segnities007.note.presentation.contract.NoteState
import com.segnities007.note.presentation.contract.NoteUserMessage
import com.segnities007.note.presentation.preview.previewNoteState
import com.segnities007.note.presentation.state.NoteScreenState
import com.segnities007.note.presentation.state.rememberNoteScreenState
import com.segnities007.note.presentation.viewmodel.NoteViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: NoteViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = uiState.currentPageIndex,
        pageCount = { uiState.pages.size }
    )
    val screenState = rememberNoteScreenState()

    ObservePendingNoteUi(
        uiState = uiState,
        onIntent = viewModel::onIntent,
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

    NoteScaffold(
        uiState = uiState,
        pagerState = pagerState,
        screenState = screenState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

/** Toast と [NoteNavigationRequest] を消費する。 */
@Composable
private fun ObservePendingNoteUi(
    uiState: NoteState,
    onIntent: (NoteIntent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(uiState.pendingUserMessage, context) {
        val message = uiState.pendingUserMessage ?: return@LaunchedEffect
        when (message) {
            is NoteUserMessage.ToastRes -> context.showShortToast(message.messageRes)
        }
        onIntent(NoteIntent.ConsumeUserMessage)
    }
    LaunchedEffect(uiState.pendingNavigation) {
        val pending = uiState.pendingNavigation ?: return@LaunchedEffect
        when (pending) {
            NoteNavigationRequest.ToSettings -> onNavigateToSettings()
            NoteNavigationRequest.ToLogin -> onNavigateToLogin()
        }
        onIntent(NoteIntent.ConsumePendingNavigation)
    }
}

/** VM の [NoteState.currentPageIndex] へ Pager の表示ページを追従させる。 */
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

/** ユーザー操作で Pager が動いたときに VM へ [NoteIntent.PageChanged] を送り、バー表示を更新する。 */
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

@NofyPreview
@Composable
private fun NoteScreenPreview() {
    val previewState = previewNoteState()

    NofyPreviewSurface {
        NoteScaffold(
            uiState = previewState,
            pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { previewState.pages.size }
            ),
            screenState = NoteScreenState(),
            onIntent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
