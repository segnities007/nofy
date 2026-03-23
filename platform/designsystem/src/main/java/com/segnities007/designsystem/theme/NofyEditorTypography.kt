package com.segnities007.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/** ノートエディタ本文用のフォントサイズと [TextStyle] ビルダー。 */
object NofyEditorTypography {
    val fontSize = 20.sp
    val lineHeight = 28.sp

    @Composable
    @ReadOnlyComposable
    fun bodyStyle(color: Color = NofyThemeTokens.colorScheme.onSurface): TextStyle {
        return NofyThemeTokens.typography.bodyLarge.copy(
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
        )
    }
}
