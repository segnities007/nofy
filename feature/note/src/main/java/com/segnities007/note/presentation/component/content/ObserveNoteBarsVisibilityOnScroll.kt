package com.segnities007.note.presentation.component.content

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

@Composable
internal fun ObserveNoteBarsVisibilityOnScroll(
    listState: LazyListState,
    onBarsVisibilityChange: (Boolean) -> Unit
) {
    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) ->
                val direction = when {
                    index != previousIndex -> {
                        if (index > previousIndex) 1 else -1
                    }

                    abs(offset - previousOffset) > 6 -> {
                        if (offset > previousOffset) 1 else -1
                    }

                    else -> 0
                }

                if (direction > 0) {
                    onBarsVisibilityChange(false)
                } else if (direction < 0) {
                    onBarsVisibilityChange(true)
                }

                previousIndex = index
                previousOffset = offset
            }
    }
}

@NofyPreview
@Composable
private fun ObserveNoteBarsVisibilityOnScrollPreview() {
    val listState = rememberLazyListState()
    var areBarsVisible by remember { mutableStateOf(true) }

    NofyPreviewSurface {
        ObserveNoteBarsVisibilityOnScroll(
            listState = listState,
            onBarsVisibilityChange = { areBarsVisible = it }
        )

        LazyColumn(state = listState) {
            item {
                NofyText(text = "Bars visible: $areBarsVisible")
            }
            items((1..20).toList()) { index ->
                NofyText(text = "Item $index")
            }
        }
    }
}
