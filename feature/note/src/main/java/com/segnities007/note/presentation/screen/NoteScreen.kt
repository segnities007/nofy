package com.segnities007.note.presentation.screen

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
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
import com.segnities007.note.presentation.component.layout.NoteScaffold
import com.segnities007.note.presentation.contract.NoteEffect
import com.segnities007.note.presentation.contract.NoteIntent
import com.segnities007.note.presentation.preview.previewNoteState
import com.segnities007.note.presentation.state.NoteScreenState
import com.segnities007.note.presentation.state.rememberNoteScreenState
import com.segnities007.note.presentation.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.Flow
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

    NoteScaffold(
        uiState = uiState,
        pagerState = pagerState,
        screenState = screenState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
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
