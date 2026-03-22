package com.segnities007.setting.presentation.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults
import com.segnities007.designsystem.atom.surface.NofyCardSurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface

@Composable
internal fun SettingsSectionList(
    content: LazyListScope.() -> Unit
) {
    val contentPadding = PaddingValues(
        start = 20.dp,
        top = 24.dp +
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            NofyFloatingBarDefaults.TopBarOverlayHeight,
        end = 20.dp,
        bottom = 24.dp +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
            NofyFloatingBarDefaults.BottomBarReservedSpace
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
