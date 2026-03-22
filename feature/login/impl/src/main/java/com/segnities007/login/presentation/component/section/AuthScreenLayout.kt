package com.segnities007.login.presentation.component.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.logo.NofyLogo
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyThemeTokens

@Composable
internal fun AuthScreenLayout(
    hero: @Composable () -> Unit,
    form: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.weight(1f))
            hero()
            Spacer(Modifier.weight(1f))
            form()
            Spacer(Modifier.weight(0.5f))
        }
    }
}

@Composable
internal fun AuthHeroSection(
    title: String,
    description: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        NofyLogo()
        NofyText(
            text = title,
            style = NofyThemeTokens.typography.titleLarge
        )
        if (!description.isNullOrBlank()) {
            NofyText(
                text = description,
                style = NofyThemeTokens.typography.bodyMedium,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
        }
    }
}
