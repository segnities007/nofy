package com.segnities007.designsystem.atom.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.theme.NofyTheme

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
        label = { Text(text = label) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            unfocusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
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
