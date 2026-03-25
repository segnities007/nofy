package com.segnities007.designsystem.atom.floatingbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.theme.NofySpacing

/** 二段フローティングバー下で LazyColumn 等に渡す [PaddingValues] を組み立てる。 */
object NofyFloatingBarScrollPadding {

    @Composable
    fun lazyListUnderDualFloatingBars(
        horizontal: Dp = NofyFloatingBarDefaults.ContentInset,
        topExtra: Dp = NofySpacing.xl,
        bottomExtra: Dp = NofySpacing.xl,
    ): PaddingValues {
        return PaddingValues(
            start = horizontal,
            top = topExtra +
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                NofyFloatingBarDefaults.TopBarOverlayHeight,
            end = horizontal,
            bottom = bottomExtra +
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                NofyFloatingBarDefaults.BottomBarReservedSpace,
        )
    }
}
