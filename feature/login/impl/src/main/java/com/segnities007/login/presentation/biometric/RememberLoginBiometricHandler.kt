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
    prompt: BiometricPromptContent
): LoginBiometricHandler {
    val activity = LocalActivity.current as? androidx.fragment.app.FragmentActivity
    val biometricAuthenticator = activity?.let { fragmentActivity ->
        koinInject<BiometricAuthenticator>(
            parameters = { parametersOf(fragmentActivity) }
        )
    }

    return remember(biometricAuthenticator, prompt) {
        createLoginBiometricHandler(
            biometricAuthenticator = biometricAuthenticator,
            prompt = prompt
        )
    }
}

@NofyPreview
@Composable
private fun RememberLoginBiometricHandlerPreview() {
    val handler = createLoginBiometricHandler(
        biometricAuthenticator = null,
        prompt = BiometricPromptContent(
            title = "Authenticate",
            subtitle = "Preview",
            failureMessage = "Unavailable"
        )
    )

    NofyPreviewSurface {
        NofyText(text = "Biometric available: ${handler.isBiometricAvailable()}")
    }
}
