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
import com.segnities007.designsystem.atom.button.NofyButton
import com.segnities007.designsystem.atom.surface.NofyFullscreenSurface
import com.segnities007.designsystem.atom.text.NofySupportingText
import com.segnities007.designsystem.atom.text.NofyTitleLargeText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.designsystem.theme.NofySpacing
import com.segnities007.designsystem.theme.NofyThemeTokens
import com.segnities007.nofy.R

@Composable
internal fun RiskyEnvironmentScreen(
    environment: RiskyEnvironment,
    onCloseApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    NofyFullscreenSurface(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(NofySpacing.xl),
            verticalArrangement = Arrangement.spacedBy(NofySpacing.lg)
        ) {
            NofyTitleLargeText(text = stringResource(R.string.security_block_title))
            NofySupportingText(
                text = stringResource(R.string.security_block_body),
                style = NofyThemeTokens.typography.bodyLarge,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(NofySpacing.stackedButtonGap)
            ) {
                environment.reasons.forEach { reason ->
                    NofySupportingText(text = reason.toDisplayText())
                }
            }
            NofySupportingText(
                text = stringResource(R.string.security_block_footer),
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
