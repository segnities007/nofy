package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.markdown.NofyMarkdown
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R

@Composable
internal fun NotePreviewPage(
    content: String,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    if (content.isBlank()) {
        NoteUnderFloatingBars.ColumnWithBottomSpacer(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = NoteUnderFloatingBars.topContentPadding()),
                contentAlignment = Alignment.Center
            ) {
                NofySupportingText(
                    text = stringResource(R.string.note_preview_empty),
                    style = NofyThemeTokens.typography.bodyLarge,
                )
            }
        }
        return
    }

    val listState = rememberLazyListState()
    ObserveNoteBarsVisibilityOnScroll(
        listState = listState,
        onBarsVisibilityChange = onBarsVisibilityChange
    )

    NoteUnderFloatingBars.LazyMainColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        NofyMarkdown(
            content = content,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NoteUnderFloatingBars.topContentPadding())
        )
    }
}

@NofyPreview
@Composable
private fun NotePreviewPagePreview() {
    NofyPreviewSurface {
        NotePreviewPage(
            content = "# Preview\n- bullet one\n- bullet two",
            onBarsVisibilityChange = {}
        )
    }
}
