package com.segnities007.login.presentation.operation

import androidx.biometric.BiometricPrompt
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.crypto.BiometricCipher

internal class PrepareBiometricUnlockOperation(
    private val authRepository: AuthRepository,
    private val biometricCipher: BiometricCipher
) {
    suspend operator fun invoke(): BiometricUnlockPreparationResult {
        val secretData = authRepository.getBiometricSecret()
            ?: return BiometricUnlockPreparationResult.MissingSecret

        val (encryptedSecret, iv) = secretData
        return try {
            BiometricUnlockPreparationResult.Ready(
                request = BiometricUnlockRequest(
                    encryptedSecret = encryptedSecret,
                    cryptoObject = BiometricPrompt.CryptoObject(
                        biometricCipher.getDecryptCipher(iv)
                    )
                )
            )
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            BiometricUnlockPreparationResult.CredentialUnavailable
        }
    }
}

internal class DecryptBiometricPasswordOperation(
    private val biometricCipher: BiometricCipher
) {
    operator fun invoke(
        request: BiometricUnlockRequest,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): BiometricPasswordDecryptionResult {
        val authenticatedCipher = authenticationResult.cryptoObject?.cipher
            ?: return BiometricPasswordDecryptionResult.Failure

        return try {
            BiometricPasswordDecryptionResult.Success(
                password = biometricCipher.decrypt(
                    request.encryptedSecret,
                    authenticatedCipher
                )
            )
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            BiometricPasswordDecryptionResult.CredentialUnavailable
        }
    }
}

internal sealed interface BiometricUnlockPreparationResult {
    data object MissingSecret : BiometricUnlockPreparationResult
    data object CredentialUnavailable : BiometricUnlockPreparationResult
    data class Ready(
        val request: BiometricUnlockRequest
    ) : BiometricUnlockPreparationResult
}

internal data class BiometricUnlockRequest(
    val encryptedSecret: ByteArray,
    val cryptoObject: BiometricPrompt.CryptoObject
)

internal sealed interface BiometricPasswordDecryptionResult {
    data class Success(
        val password: String
    ) : BiometricPasswordDecryptionResult

    data object CredentialUnavailable : BiometricPasswordDecryptionResult
    data object Failure : BiometricPasswordDecryptionResult
}
