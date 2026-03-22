package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults

internal val NotePageInnerContentPadding = 12.dp
internal val NotePageBottomContentPadding = NofyFloatingBarDefaults.BottomBarReservedSpace +
    NotePageInnerContentPadding

@Composable
internal fun notePageTopContentPadding(): Dp {
    return WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
        NofyFloatingBarDefaults.TopBarOverlayHeight +
        NotePageInnerContentPadding
}

@Composable
internal fun NotePageBottomSpacer(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(NotePageBottomContentPadding))
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
