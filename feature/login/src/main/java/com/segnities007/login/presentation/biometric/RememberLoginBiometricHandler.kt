package com.segnities007.login.presentation.biometric

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.segnities007.designsystem.atom.text.NofyText
import com.segnities007.designsystem.theme.NofyPreview
import com.segnities007.designsystem.theme.NofyPreviewSurface
import com.segnities007.biometric.BiometricAuthenticator
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
internal fun rememberLoginBiometricHandler(
    authenticatePrompt: BiometricPromptContent,
    cryptoPrompt: BiometricPromptContent = authenticatePrompt
): LoginBiometricHandler {
    val activity = LocalActivity.current as? androidx.fragment.app.FragmentActivity
    val biometricAuthenticator = activity?.let { fragmentActivity ->
        koinInject<BiometricAuthenticator>(
            parameters = { parametersOf(fragmentActivity) }
        )
    }

    return remember(biometricAuthenticator, authenticatePrompt, cryptoPrompt) {
        createLoginBiometricHandler(
            biometricAuthenticator = biometricAuthenticator,
            authenticatePrompt = authenticatePrompt,
            cryptoPrompt = cryptoPrompt
        )
    }
}

@NofyPreview
@Composable
private fun RememberLoginBiometricHandlerPreview() {
    val handler = createLoginBiometricHandler(
        biometricAuthenticator = null,
        authenticatePrompt = BiometricPromptContent(
            title = "Authenticate",
            subtitle = "Preview",
            failureMessage = "Unavailable"
        )
    )

    NofyPreviewSurface {
        NofyText(text = "Biometric available: ${handler.isBiometricAvailable()}")
    }
}
