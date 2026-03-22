package com.segnities007.setting.presentation.biometric

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.segnities007.biometric.BiometricAuthenticator
import com.segnities007.crypto.BiometricCipher
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.resume

internal data class SettingEncryptedBiometricSecret(
    val encryptedSecret: ByteArray,
    val iv: ByteArray
)

internal data class SettingBiometricPromptContent(
    val title: String,
    val subtitle: String,
    val unavailableMessage: String
)

internal sealed interface SettingBiometricEnrollmentResult {
    data class Success(
        val secret: SettingEncryptedBiometricSecret
    ) : SettingBiometricEnrollmentResult

    data class Failure(
        val message: String
    ) : SettingBiometricEnrollmentResult

    data object CredentialUnavailable : SettingBiometricEnrollmentResult
}

private sealed interface BiometricAuthenticationAttempt {
    data class Success(
        val result: BiometricPrompt.AuthenticationResult
    ) : BiometricAuthenticationAttempt

    data class Failure(
        val message: String
    ) : BiometricAuthenticationAttempt
}

@Composable
internal fun rememberSettingBiometricEnrollmentController(
    promptContent: SettingBiometricPromptContent
): SettingBiometricEnrollmentController {
    val activity = LocalActivity.current as? androidx.fragment.app.FragmentActivity
    val biometricAuthenticator = activity?.let { fragmentActivity ->
        koinInject<BiometricAuthenticator>(
            parameters = { parametersOf(fragmentActivity) }
        )
    }
    val biometricCipher = koinInject<BiometricCipher>()

    return remember(biometricAuthenticator, biometricCipher, promptContent) {
        SettingBiometricEnrollmentController(
            biometricAuthenticator = biometricAuthenticator,
            biometricCipher = biometricCipher,
            promptContent = promptContent
        )
    }
}

internal class SettingBiometricEnrollmentController(
    private val biometricAuthenticator: BiometricAuthenticator?,
    private val biometricCipher: BiometricCipher,
    private val promptContent: SettingBiometricPromptContent
) {
    suspend fun enroll(password: ByteArray): SettingBiometricEnrollmentResult {
        val authenticator = biometricAuthenticator
            ?: return SettingBiometricEnrollmentResult.Failure(promptContent.unavailableMessage)
        if (!authenticator.isStrongBiometricAvailable()) {
            return SettingBiometricEnrollmentResult.Failure(promptContent.unavailableMessage)
        }

        val cryptoObject = try {
            BiometricPrompt.CryptoObject(biometricCipher.getEncryptCipher())
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            return SettingBiometricEnrollmentResult.CredentialUnavailable
        }

        val authenticationResult = authenticate(authenticator, cryptoObject)
        val authenticated = when (authenticationResult) {
            is BiometricAuthenticationAttempt.Success -> authenticationResult.result
            is BiometricAuthenticationAttempt.Failure -> {
                return SettingBiometricEnrollmentResult.Failure(authenticationResult.message)
            }
        }
        val authenticatedCipher = authenticated.cryptoObject?.cipher
            ?: return SettingBiometricEnrollmentResult.Failure(promptContent.unavailableMessage)

        return try {
            val (encryptedSecret, iv) = biometricCipher.encrypt(password, authenticatedCipher)
            SettingBiometricEnrollmentResult.Success(
                secret = SettingEncryptedBiometricSecret(
                    encryptedSecret = encryptedSecret,
                    iv = iv
                )
            )
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            SettingBiometricEnrollmentResult.CredentialUnavailable
        }
    }

    private suspend fun authenticate(
        authenticator: BiometricAuthenticator,
        cryptoObject: BiometricPrompt.CryptoObject
    ): BiometricAuthenticationAttempt = suspendCancellableCoroutine { continuation ->
        authenticator.authenticate(
            title = promptContent.title,
            subtitle = promptContent.subtitle,
            cryptoObject = cryptoObject,
            onSuccess = { result ->
                if (continuation.isActive) {
                    continuation.resume(BiometricAuthenticationAttempt.Success(result))
                }
            },
            onError = { _, errString ->
                if (continuation.isActive) {
                    continuation.resume(
                        BiometricAuthenticationAttempt.Failure(errString.toString())
                    )
                }
            },
            onFailed = {
                if (continuation.isActive) {
                    continuation.resume(
                        BiometricAuthenticationAttempt.Failure(promptContent.unavailableMessage)
                    )
                }
            }
        )
    }
}
