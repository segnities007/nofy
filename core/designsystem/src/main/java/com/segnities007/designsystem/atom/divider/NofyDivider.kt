package com.segnities007.designsystem.atom.divider

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyTheme

@Composable
fun NofyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier)
}

@Preview
@Composable
private fun NofyDividerPreview() {
    NofyTheme {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NofyDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
