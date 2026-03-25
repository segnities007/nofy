package com.segnities007.designsystem.atom.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.input.consumeObscuredTouches
import com.segnities007.designsystem.theme.NofyElevation
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rejectObscuredTouches: Boolean = false,
    onObscuredTouch: (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .consumeObscuredTouches(
                enabled = rejectObscuredTouches,
                onBlocked = onObscuredTouch
            )
            .fillMaxWidth()
            .height(NofySpacing.primaryButtonMinHeight),
        enabled = enabled,
        shape = NofyThemeTokens.shapes.large,
        contentPadding = ButtonDefaults.ContentPadding,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = NofyElevation.none,
            pressedElevation = NofyElevation.none,
            disabledElevation = NofyElevation.none,
        )
    ) {
        NofyText(
            text = text,
            style = NofyThemeTokens.typography.titleMedium
        )
    }
}

@NofyPreview
@Composable
private fun NofyButtonPreview() {
    NofyPreviewSurface {
        NofyButton(
            text = "Login",
            onClick = {},
            modifier = Modifier.padding(NofySpacing.previewCanvasPadding)
        )
    }
}
