package com.segnities007.designsystem.atom.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyTitleLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    NofyText(
        text = text,
        modifier = modifier,
        style = NofyThemeTokens.typography.titleLarge,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
    )
}

@Composable
fun NofySupportingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = NofyThemeTokens.typography.bodyMedium,
    color: Color = NofyThemeTokens.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    NofyText(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
    )
}

@NofyPreview
@Composable
private fun NofySemanticTextPreview() {
    NofyPreviewSurface {
        NofyTitleLargeText(text = "Title")
        NofySupportingText(text = "Supporting copy uses onSurfaceVariant by default.")
    }
}
