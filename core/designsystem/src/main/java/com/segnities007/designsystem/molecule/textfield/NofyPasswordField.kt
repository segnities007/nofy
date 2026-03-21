package com.segnities007.designsystem.molecule.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.segnities007.designsystem.atom.textfield.NofyTextField
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    NofyTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        visualTransformation = PasswordVisualTransformation()
    )
}

@Preview
@Composable
private fun NofyPasswordFieldPreview() {
    NofyTheme {
        NofyPasswordField(
            value = "",
            onValueChange = {},
            label = "Password"
        )
    }
}
