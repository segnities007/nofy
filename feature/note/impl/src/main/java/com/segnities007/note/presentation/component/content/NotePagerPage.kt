package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.presentation.preview.previewNotePages
import com.segnities007.note.presentation.state.NotePageUiState

@Composable
internal fun NotePagerPage(
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
                .padding(
                    start = NofyFloatingBarDefaults.ContentInset,
                    end = NofyFloatingBarDefaults.ContentInset
                )
        ) {
            if (isPreviewEnabled) {
                NotePreviewPage(
                    content = page.content,
                    onBarsVisibilityChange = onBarsVisibilityChange
                )
            } else {
                NoteEditorPage(
                    content = page.content,
                    onContentChange = onContentChange,
                    onBarsVisibilityChange = onBarsVisibilityChange
                )
            }
        }
    }
}

@NofyPreview
@Composable
private fun NotePagerPagePreview() {
    NofyPreviewSurface {
        NotePagerPage(
            page = previewNotePages().first(),
            isPreviewEnabled = false,
            onContentChange = {},
            onBarsVisibilityChange = {}
        )
    }
}
