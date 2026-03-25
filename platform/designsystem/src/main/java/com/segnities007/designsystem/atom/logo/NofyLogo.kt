package com.segnities007.designsystem.atom.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.R
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
fun NofyLogo(
    modifier: Modifier = Modifier,
    size: Dp = NofySpacing.logoMarkSize
) {
    Image(
        painter = painterResource(id = R.drawable.ic_login_logo),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

@NofyPreview
@Composable
private fun NofyLogoPreview() {
    NofyPreviewSurface {
        NofyLogo(modifier = Modifier.padding(NofySpacing.previewCanvasPadding))
    }
}
