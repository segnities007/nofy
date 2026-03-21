package com.segnities007.setting.presentation

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.theme.NofyTheme
import com.segnities007.settings.UiSettingsRepository
import kotlinx.coroutines.flow.Flow

@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    uiSettingsRepository: UiSettingsRepository,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingViewModel = viewModel(
        factory = settingViewModelFactory(
            authRepository = authRepository,
            uiSettingsRepository = uiSettingsRepository
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveSettingEffects(
        effect = viewModel.effect,
        onNavigateToLogin = onNavigateToLogin
    )

    NofySurface(modifier = modifier.fillMaxSize()) {
        SettingsContent(
            uiState = uiState,
            onIntent = viewModel::onIntent,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun ObserveSettingEffects(
    effect: Flow<SettingEffect>,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(effect, context) {
        effect.collect { handledEffect ->
            when (handledEffect) {
                is SettingEffect.ShowToastRes -> showToast(context, handledEffect.messageRes)
                SettingEffect.NavigateToLogin -> onNavigateToLogin()
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

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    NofyTheme {
        NofySurface {
            SettingsContent(
                uiState = SettingState(),
                onIntent = {},
                onNavigateBack = {}
            )
        }
    }
}
