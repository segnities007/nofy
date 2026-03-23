package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarContentInsets

private enum class NoteScrollSlot {
    Header,
    Main,
    Footer,
    BottomSpacer,
}

private const val NoteLazyBottomSpacerKey = "note_under_floating_bars_bottom_spacer"

/** フローティングバー下の本文用インセットと LazyColumn／Scroll のスペーサ。 */
internal object NoteUnderFloatingBars {

    val bottomContentInset: Dp
        get() = NofyFloatingBarContentInsets.bottomBelowFloatingBar

    @Composable
    fun topContentPadding(): Dp = NofyFloatingBarContentInsets.topBelowFloatingTopBar()

    @Composable
    fun BottomSpacer(modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Spacer(modifier = Modifier.height(bottomContentInset))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }

    @Composable
    fun LazyMainColumn(
        state: LazyListState,
        modifier: Modifier = Modifier,
        header: (@Composable () -> Unit)? = null,
        footer: (@Composable () -> Unit)? = null,
        main: @Composable () -> Unit,
    ) {
        LazyColumn(state = state, modifier = modifier) {
            noteLazySlots(header = header, footer = footer, main = main)
        }
    }

    @Composable
    fun ColumnWithBottomSpacer(
        modifier: Modifier = Modifier,
        scrollState: ScrollState? = null,
        header: (@Composable () -> Unit)? = null,
        footer: (@Composable () -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        val m = if (scrollState != null) modifier.verticalScroll(scrollState) else modifier
        Column(modifier = m) {
            header?.invoke()
            content()
            footer?.invoke()
            BottomSpacer()
        }
    }
}

private fun LazyListScope.noteLazySlots(
    header: (@Composable () -> Unit)?,
    footer: (@Composable () -> Unit)?,
    main: @Composable () -> Unit,
) {
    if (header != null) {
        item(contentType = NoteScrollSlot.Header) { header() }
    }
    item(contentType = NoteScrollSlot.Main) { main() }
    if (footer != null) {
        item(contentType = NoteScrollSlot.Footer) { footer() }
    }
    item(
        contentType = NoteScrollSlot.BottomSpacer,
        key = NoteLazyBottomSpacerKey,
    ) {
        NoteUnderFloatingBars.BottomSpacer()
    }
}
