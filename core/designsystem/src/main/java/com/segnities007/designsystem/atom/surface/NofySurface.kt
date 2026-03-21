package com.segnities007.designsystem.atom.surface

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofySurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun NofySurfacePreview() {
    NofyTheme {
        NofySurface(modifier = Modifier.fillMaxSize()) {
            NofyText(
                text = "Surface",
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
