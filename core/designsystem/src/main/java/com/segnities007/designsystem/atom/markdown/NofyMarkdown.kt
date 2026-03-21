package com.segnities007.designsystem.atom.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyMarkdown(
    content: String,
    modifier: Modifier = Modifier
) {
    Markdown(
        content = content,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun NofyMarkdownPreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
