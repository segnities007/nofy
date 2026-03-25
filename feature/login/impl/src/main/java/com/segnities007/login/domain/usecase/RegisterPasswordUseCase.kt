package com.segnities007.login.domain.usecase

import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository

/** 初回マスターパスワード登録。ポリシー違反は [PasswordRegistrationResult] で返す。 */
internal class RegisterPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(password: String): PasswordRegistrationResult {
        val result = authRepository.registerPassword(password)
        if (result.isSuccess) {
            return PasswordRegistrationResult.Success
        }

        return when (val error = result.exceptionOrNull()) {
            is AuthException.PasswordTooShort -> PasswordRegistrationResult.TooShort(error.minimumLength)
            AuthException.PasswordTooCommon -> PasswordRegistrationResult.TooCommon
            AuthException.UntrustedEnvironment -> PasswordRegistrationResult.UntrustedEnvironment
            else -> PasswordRegistrationResult.Failure
        }
    }
}

/** 生体プロンプト後に得た暗号化シークレットを [AuthRepository] へ保存する。 */
internal class SaveBiometricSecretUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        encryptedSecret: ByteArray,
        iv: ByteArray
    ): BiometricSecretSaveResult {
        val result = authRepository.saveBiometricSecret(encryptedSecret, iv)
        return if (result.isSuccess) {
            BiometricSecretSaveResult.Success
        } else if (result.exceptionOrNull() == AuthException.UntrustedEnvironment) {
            BiometricSecretSaveResult.UntrustedEnvironment
        } else {
            BiometricSecretSaveResult.Failure
        }
    }
}

/**
 * 初回パスワード登録の結果（ポリシー違反・環境不可を型で区別）。
 */
internal sealed interface PasswordRegistrationResult {
    /** 登録に成功した。 */
    data object Success : PasswordRegistrationResult

    /** 最小文字数未満。[minimumLength] を UI に示す。 */
    data class TooShort(val minimumLength: Int) : PasswordRegistrationResult

    /** ポリシー上禁止されている単純すぎるパスワード。 */
    data object TooCommon : PasswordRegistrationResult

    /** 端末が信頼できず登録を拒否された。 */
    data object UntrustedEnvironment : PasswordRegistrationResult

    /** 上記以外の失敗。 */
    data object Failure : PasswordRegistrationResult
}

/**
 * 生体用暗号化シークレットの永続化の結果。
 */
internal sealed interface BiometricSecretSaveResult {
    /** シークレットの保存に成功した。 */
    data object Success : BiometricSecretSaveResult

    /** 信頼できない環境のため保存できない。 */
    data object UntrustedEnvironment : BiometricSecretSaveResult

    /** その他の保存失敗。 */
    data object Failure : BiometricSecretSaveResult
}
