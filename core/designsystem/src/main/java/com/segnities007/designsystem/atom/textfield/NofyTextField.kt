package com.segnities007.designsystem.atom.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
fun NofyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
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
        modifier = modifier.fillMaxWidth(),
        textStyle = NofyThemeTokens.typography.bodyLarge,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        shape = NofyThemeTokens.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NofyThemeTokens.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = NofyThemeTokens.colorScheme.surfaceContainerLow,
            focusedBorderColor = NofyThemeTokens.colorScheme.primary.copy(alpha = 0.6f),
            unfocusedBorderColor = NofyThemeTokens.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    )
}

@Preview
@Composable
private fun NofyTextFieldPreview() {
    NofyTheme {
        NofyTextField(
            value = "",
            onValueChange = {},
            label = "Password"
        )
    }
}
