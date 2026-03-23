package com.segnities007.designsystem.atom.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.segnities007.designsystem.input.consumeObscuredTouches
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    rejectObscuredTouches: Boolean = false,
    onObscuredTouch: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = NofyThemeTokens.typography.bodyMedium
            )
        },
        modifier = modifier
            .consumeObscuredTouches(
                enabled = rejectObscuredTouches,
                onBlocked = onObscuredTouch
            )
            .fillMaxWidth(),
        textStyle = NofyThemeTokens.typography.bodyLarge,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        shape = NofyThemeTokens.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NofyThemeTokens.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = NofyThemeTokens.colorScheme.surfaceContainerLow,
            focusedBorderColor = NofyThemeTokens.colorScheme.primary.copy(alpha = 0.6f),
            unfocusedBorderColor = NofyThemeTokens.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    )
}

@NofyPreview
@Composable
private fun NofyTextFieldPreview() {
    NofyPreviewSurface {
        NofyTextField(
            value = "",
            onValueChange = {},
            label = "Password",
            modifier = Modifier
                .padding(NofySpacing.previewCanvasPadding)
                .fillMaxWidth()
        )
    }
}
