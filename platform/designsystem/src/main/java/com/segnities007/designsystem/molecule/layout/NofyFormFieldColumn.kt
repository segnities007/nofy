package com.segnities007.designsystem.molecule.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.molecule.textfield.NofyPasswordField
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyFormFieldColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NofySpacing.formFieldGap),
        content = content,
    )
}

@NofyPreview
@Composable
private fun NofyFormFieldColumnPreview() {
    NofyPreviewSurface {
        NofyFormFieldColumn {
            NofyPasswordField(value = "", onValueChange = {}, label = "Password")
            NofyButton(text = "Submit", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
