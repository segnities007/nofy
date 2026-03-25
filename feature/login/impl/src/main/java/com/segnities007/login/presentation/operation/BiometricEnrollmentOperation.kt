package com.segnities007.login.presentation.operation

import androidx.biometric.BiometricPrompt
import com.segnities007.crypto.BiometricCipher

/** 登録時にマスターパスワードを暗号化するための [BiometricPrompt.CryptoObject] を用意する。 */
internal class PrepareBiometricEnrollmentOperation(
    private val biometricCipher: BiometricCipher
) {
    operator fun invoke(): BiometricEnrollmentPreparationResult {
        return try {
            BiometricEnrollmentPreparationResult.Ready(
                request = BiometricEnrollmentRequest(
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
        password: ByteArray,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): BiometricSecretEncryptionResult {
        val authenticatedCipher = authenticationResult.cryptoObject?.cipher
            ?: return BiometricSecretEncryptionResult.Failure

        return try {
            val (encryptedSecret, iv) = biometricCipher.encrypt(
                password,
                authenticatedCipher
            )
            BiometricSecretEncryptionResult.Success(
                secret = EncryptedBiometricSecret(
                    encryptedSecret = encryptedSecret,
                    iv = iv
                )
            )
        } catch (_: BiometricCipher.CredentialUnavailableException) {
            BiometricSecretEncryptionResult.Failure
        }
    }
}

/** 生体登録プロンプトに渡す暗号化用 CryptoObject のみを保持する。 */
internal data class BiometricEnrollmentRequest(
    val cryptoObject: BiometricPrompt.CryptoObject
)

/**
 * 登録フローで暗号化用 Cipher を用意できたかどうか。
 */
internal sealed interface BiometricEnrollmentPreparationResult {
    data class Ready(
        val request: BiometricEnrollmentRequest
    ) : BiometricEnrollmentPreparationResult

    data object Failure : BiometricEnrollmentPreparationResult
}

/** [SaveBiometricSecretUseCase] へ渡す暗号文と IV。 */
internal data class EncryptedBiometricSecret(
    val encryptedSecret: ByteArray,
    val iv: ByteArray
)

/**
 * 認証済み Cipher でパスワードバイト列を暗号化した結果。
 */
internal sealed interface BiometricSecretEncryptionResult {
    data class Success(
        val secret: EncryptedBiometricSecret
    ) : BiometricSecretEncryptionResult

    data object Failure : BiometricSecretEncryptionResult
}
