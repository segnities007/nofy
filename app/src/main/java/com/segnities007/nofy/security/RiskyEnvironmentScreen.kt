package com.segnities007.nofy.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.surface.NofySurface
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.nofy.R

@Composable
internal fun RiskyEnvironmentScreen(
    environment: RiskyEnvironment,
    onCloseApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    NofySurface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NofyText(
                text = stringResource(R.string.security_block_title),
                style = NofyThemeTokens.typography.titleLarge
            )
            NofyText(
                text = stringResource(R.string.security_block_body),
                style = NofyThemeTokens.typography.bodyLarge,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                environment.reasons.forEach { reason ->
                    NofyText(
                        text = reason.toDisplayText(),
                        style = NofyThemeTokens.typography.bodyMedium,
                        color = NofyThemeTokens.colorScheme.onSurfaceVariant
                    )
                }
            }
            NofyText(
                text = stringResource(R.string.security_block_footer),
                style = NofyThemeTokens.typography.bodyMedium,
                color = NofyThemeTokens.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
            NofyButton(
                text = stringResource(R.string.security_block_close),
                onClick = onCloseApp
            )
        }
    }
}

@Composable
private fun RiskyEnvironmentReason.toDisplayText(): String {
    return when (this) {
        RiskyEnvironmentReason.DebuggerAttached -> stringResource(R.string.security_reason_debugger)
        RiskyEnvironmentReason.ProcessTraced -> stringResource(R.string.security_reason_tracer)
        RiskyEnvironmentReason.FridaServerDetected -> stringResource(R.string.security_reason_frida_port)
        RiskyEnvironmentReason.InjectedHookLibraryDetected -> stringResource(R.string.security_reason_hook_library)
        RiskyEnvironmentReason.HookFrameworkPackageDetected -> stringResource(R.string.security_reason_hook_package)
        RiskyEnvironmentReason.SuspiciousEnvironmentVariable -> stringResource(R.string.security_reason_env_var)
        RiskyEnvironmentReason.UnexpectedDebuggableApp -> stringResource(R.string.security_reason_debuggable)
        RiskyEnvironmentReason.TestKeysBuild -> stringResource(R.string.security_reason_test_keys)
        RiskyEnvironmentReason.WritableSystemPartition -> stringResource(R.string.security_reason_writable_system)
        RiskyEnvironmentReason.PermissiveSelinux -> stringResource(R.string.security_reason_selinux)
        RiskyEnvironmentReason.RootArtifactDetected -> stringResource(R.string.security_reason_root_artifact)
        RiskyEnvironmentReason.RootManagerDetected -> stringResource(R.string.security_reason_root_manager)
    }
}

@NofyPreview
@Composable
private fun RiskyEnvironmentScreenPreview() {
    NofyPreviewSurface {
        RiskyEnvironmentScreen(
            environment = RiskyEnvironment(
                reasons = listOf(
                    RiskyEnvironmentReason.RootArtifactDetected,
                    RiskyEnvironmentReason.DebuggerAttached
                )
            ),
            onCloseApp = {}
        )
    }
}
