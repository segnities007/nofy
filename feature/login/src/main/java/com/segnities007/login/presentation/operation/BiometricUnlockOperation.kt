package com.segnities007.login.presentation.operation

import androidx.biometric.BiometricPrompt
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.crypto.BiometricCipher
import com.segnities007.login.domain.usecase.LoginSubmissionResult
import com.segnities007.login.domain.usecase.UnlockWithBiometricUseCase
import java.nio.charset.StandardCharsets

internal class PrepareBiometricUnlockOperation(
    private val authRepository: AuthRepository,
    private val biometricCipher: BiometricCipher
) {
    suspend operator fun invoke(): BiometricUnlockPreparationResult {
        val secretData = try {
            authRepository.getBiometricSecret()
        } catch (error: AuthException) {
            if (error == AuthException.UntrustedEnvironment) {
                return BiometricUnlockPreparationResult.UntrustedEnvironment
            }
            throw error
        } ?: return BiometricUnlockPreparationResult.MissingSecret

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
    private val biometricCipher: BiometricCipher,
    private val unlockWithBiometricUseCase: UnlockWithBiometricUseCase
) {
    suspend operator fun invoke(
        request: BiometricUnlockRequest,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): BiometricPasswordDecryptionResult {
        val authenticatedCipher = authenticationResult.cryptoObject?.cipher
            ?: return BiometricPasswordDecryptionResult.Failure

        return try {
            val passwordBytes = biometricCipher.decryptToByteArray(
                request.encryptedSecret,
                authenticatedCipher
            )
            try {
                val password = String(passwordBytes, StandardCharsets.UTF_8)
                BiometricPasswordDecryptionResult.Success(
                    submissionResult = unlockWithBiometricUseCase(password)
                )
            } finally {
                passwordBytes.fill(0)
            }
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            BiometricPasswordDecryptionResult.CredentialUnavailable
        }
    }
}

internal sealed interface BiometricUnlockPreparationResult {
    data object MissingSecret : BiometricUnlockPreparationResult
    data object CredentialUnavailable : BiometricUnlockPreparationResult
    data object UntrustedEnvironment : BiometricUnlockPreparationResult
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
        val submissionResult: LoginSubmissionResult
    ) : BiometricPasswordDecryptionResult

    data object CredentialUnavailable : BiometricPasswordDecryptionResult
    data object Failure : BiometricPasswordDecryptionResult
}
