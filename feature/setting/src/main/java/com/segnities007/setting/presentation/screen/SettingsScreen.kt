package com.segnities007.setting.presentation.screen

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.setting.presentation.component.layout.SettingsScaffold
import com.segnities007.setting.presentation.contract.SettingEffect
import com.segnities007.setting.presentation.preview.previewSettingState
import com.segnities007.setting.presentation.viewmodel.SettingViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveSettingEffects(
        effect = viewModel.effect,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToSignUp = onNavigateToSignUp
    )

    NofySurface(modifier = modifier.fillMaxSize()) {
        SettingsScaffold(
            uiState = uiState,
            onIntent = viewModel::onIntent,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun ObserveSettingEffects(
    effect: Flow<SettingEffect>,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(effect, context) {
        effect.collect { handledEffect ->
            when (handledEffect) {
                is SettingEffect.ShowToastRes -> showToast(context, handledEffect.messageRes)
                SettingEffect.NavigateToLogin -> onNavigateToLogin()
                SettingEffect.NavigateToSignUp -> onNavigateToSignUp()
            }
        }
    }
}

private fun showToast(
    context: Context,
    @StringRes messageRes: Int
) {
    Toast.makeText(
        context,
        context.getString(messageRes),
        Toast.LENGTH_SHORT
    ).show()
}

@NofyPreview
@Composable
private fun SettingsScreenPreview() {
    NofyPreviewSurface {
        SettingsScaffold(
            uiState = previewSettingState(),
            onIntent = {},
            onNavigateBack = {}
        )
    }
}
