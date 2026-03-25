package com.segnities007.designsystem.atom.toggle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.input.consumeObscuredTouches
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rejectObscuredTouches: Boolean = false,
    onObscuredTouch: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = semanticsModifier
            .then(modifier)
            .consumeObscuredTouches(
                enabled = rejectObscuredTouches,
                onBlocked = onObscuredTouch
            ),
        enabled = enabled
    )
}

@NofyPreview
@Composable
private fun NofySwitchPreview() {
    NofyPreviewSurface {
        NofySurface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(NofySpacing.previewCanvasPadding),
                contentAlignment = Alignment.Center
            ) {
                var checked by remember { mutableStateOf(true) }
                NofySwitch(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }
        }
    }
}
