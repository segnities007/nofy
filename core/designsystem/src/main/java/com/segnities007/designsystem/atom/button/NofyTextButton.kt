package com.segnities007.designsystem.atom.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.input.consumeObscuredTouches
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rejectObscuredTouches: Boolean = false,
    onObscuredTouch: (() -> Unit)? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.consumeObscuredTouches(
            enabled = rejectObscuredTouches,
            onBlocked = onObscuredTouch
        ),
        enabled = enabled
    ) {
        NofyText(text = text)
    }
}

@Preview
@Composable
private fun NofyTextButtonPreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NofyTextButton(
                    text = "Cancel",
                    onClick = {}
                )
            }
        }
    }
}
