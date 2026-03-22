package com.segnities007.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.segnities007.designsystem.atom.text.NofyText

object NofyThemeTokens {
    val colorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}

@NofyPreview
@Composable
private fun NofyThemeTokensPreview() {
    NofyPreviewSurface {
        NofyText(
            text = "Theme tokens preview",
            color = NofyThemeTokens.colorScheme.onSurface
        )
    }
}
