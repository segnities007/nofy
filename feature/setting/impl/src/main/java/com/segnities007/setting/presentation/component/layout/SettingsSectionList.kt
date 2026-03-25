package com.segnities007.setting.presentation.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarScrollPadding
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing

@Composable
internal fun SettingsSectionList(
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = NofyFloatingBarScrollPadding.lazyListUnderDualFloatingBars(),
        verticalArrangement = Arrangement.spacedBy(NofySpacing.sectionCardGap),
        content = content
    )
}

@NofyPreview
@Composable
private fun SettingsSectionListPreview() {
    NofyPreviewSurface {
        SettingsSectionList {
            item {
                NofyCardSurface {
                    NofyText(text = "Section one")
                }
            }
            item {
                NofyCardSurface {
                    NofyText(text = "Section two")
                }
            }
        }
    }
}
