package com.segnities007.designsystem.atom.logo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.R

@Composable
fun NofyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_login_logo),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

@Preview
@Composable
private fun NofyLogoPreview() {
    NofyLogo()
}
