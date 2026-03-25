package com.segnities007.designsystem.atom.floatingbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.theme.NofySpacing

/** フローティング上下バーとステータスバーを考慮した本文側の余白（Dp）。 */
object NofyFloatingBarContentInsets {
    private val innerGap: Dp = NofySpacing.md

    /** Reserve space below a floating bottom bar plus a small gap (e.g. list bottom spacer). */
    val bottomBelowFloatingBar: Dp
        get() = NofyFloatingBarDefaults.BottomBarReservedSpace + innerGap

    @Composable
    fun topBelowFloatingTopBar(): Dp =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            NofyFloatingBarDefaults.TopBarOverlayHeight +
            innerGap
}
