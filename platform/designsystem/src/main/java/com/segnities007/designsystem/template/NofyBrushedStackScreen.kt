package com.segnities007.designsystem.template

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarContentInsets
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.surface.NofyFullscreenSurface
import com.segnities007.designsystem.molecule.bar.NofyBackFloatingTopBar
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofySpacing

private val stackBodyBottomPadding = NofySpacing.xl

@Composable
fun NofyBrushedStackScreen(
    title: String,
    onNavigateBack: () -> Unit,
    backContentDescription: String,
    modifier: Modifier = Modifier,
    showEdgeBrushes: Boolean = false,
    overlay: @Composable BoxScope.() -> Unit = {},
    body: @Composable ColumnScope.() -> Unit,
) {
    NofyFullscreenSurface(modifier = modifier) {
        NofyBrushedFloatingBarScreen(
            modifier = Modifier.fillMaxSize(),
            showEdgeBrushes = showEdgeBrushes,
            body = {
                val scroll = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(scroll)
                        .padding(top = NofyFloatingBarContentInsets.topBelowFloatingTopBar())
                        .padding(horizontal = NofyFloatingBarDefaults.ContentInset)
                        .padding(bottom = stackBodyBottomPadding)
                ) {
                    body()
                }
            },
            header = { topModifier ->
                NofyBackFloatingTopBar(
                    title = title,
                    onNavigateBack = onNavigateBack,
                    backContentDescription = backContentDescription,
                    modifier = topModifier,
                )
            },
            footer = { _ -> },
            overlay = overlay,
        )
    }
}

@NofyPreview
@Composable
private fun NofyBrushedStackScreenPreview() {
    NofyPreviewSurface {
        NofyBrushedStackScreen(
            title = "Stack",
            onNavigateBack = {},
            backContentDescription = "Back",
        ) {
            NofyText(text = "Body")
        }
    }
}
