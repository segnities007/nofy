package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.segnities007.designsystem.atom.markdown.NofyMarkdown
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.note.R

@Composable
internal fun NotePreviewPage(
    content: String,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    ObserveNoteBarsVisibilityOnScroll(
        listState = listState,
        onBarsVisibilityChange = onBarsVisibilityChange
    )

    if (content.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = notePageTopContentPadding(),
                    bottom = NotePageBottomContentPadding
                ),
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
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        item {
            NofyMarkdown(
                content = content,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = notePageTopContentPadding())
            )
        }

        item {
            NotePageBottomSpacer()
        }
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
