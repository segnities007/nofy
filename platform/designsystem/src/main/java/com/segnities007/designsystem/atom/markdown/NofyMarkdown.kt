package com.segnities007.designsystem.atom.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown
import com.segnities007.designsystem.theme.NofyEditorTypography
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyMarkdown(
    content: String,
    modifier: Modifier = Modifier
) {
    ProvideTextStyle(NofyEditorTypography.bodyStyle()) {
        Markdown(
            content = content,
            modifier = modifier
        )
    }
}

@NofyPreview
@Composable
private fun NofyMarkdownPreview() {
    NofyPreviewSurface {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NofySpacing.previewCanvasPadding),
                contentAlignment = Alignment.TopStart
            ) {
                NofyMarkdown(
                    content = "# Heading\n\n- first item\n- second item\n\n`inline code`",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
