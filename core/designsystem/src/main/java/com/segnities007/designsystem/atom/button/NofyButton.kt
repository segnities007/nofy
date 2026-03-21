package com.segnities007.designsystem.atom.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        contentPadding = ButtonDefaults.ContentPadding,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        NofyText(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun NofyButtonPreview() {
    NofyTheme {
        NofyButton(
            text = "Login",
            onClick = {}
        )
    }
}
