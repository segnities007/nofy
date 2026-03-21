package com.segnities007.login.presentation.operation

import androidx.biometric.BiometricPrompt
import com.segnities007.crypto.BiometricCipher

internal class PrepareBiometricEnrollmentOperation(
    private val biometricCipher: BiometricCipher
) {
    operator fun invoke(password: String): BiometricEnrollmentPreparationResult {
        return try {
            BiometricEnrollmentPreparationResult.Ready(
                request = BiometricEnrollmentRequest(
                    password = password,
                    cryptoObject = BiometricPrompt.CryptoObject(
                        biometricCipher.getEncryptCipher()
                    )
                )
            )
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            BiometricEnrollmentPreparationResult.Failure
        }
    }
}

internal class EncryptBiometricSecretOperation(
    private val biometricCipher: BiometricCipher
) {
    operator fun invoke(
        request: BiometricEnrollmentRequest,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): BiometricSecretEncryptionResult {
        val authenticatedCipher = authenticationResult.cryptoObject?.cipher
            ?: return BiometricSecretEncryptionResult.Failure

        val (encryptedSecret, iv) = biometricCipher.encrypt(
            request.password,
            authenticatedCipher
        )
        return BiometricSecretEncryptionResult.Success(
            secret = EncryptedBiometricSecret(
                encryptedSecret = encryptedSecret,
                iv = iv
            )
        )
    }
}

internal data class BiometricEnrollmentRequest(
    val password: String,
    val cryptoObject: BiometricPrompt.CryptoObject
)

internal sealed interface BiometricEnrollmentPreparationResult {
    data class Ready(
        val request: BiometricEnrollmentRequest
    ) : BiometricEnrollmentPreparationResult

    data object Failure : BiometricEnrollmentPreparationResult
}

internal data class EncryptedBiometricSecret(
    val encryptedSecret: ByteArray,
    val iv: ByteArray
)

internal sealed interface BiometricSecretEncryptionResult {
    data class Success(
        val secret: EncryptedBiometricSecret
    ) : BiometricSecretEncryptionResult

    data object Failure : BiometricSecretEncryptionResult
}
