package com.segnities007.login.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.login.R
import com.segnities007.login.domain.usecase.BiometricSecretSaveResult
import com.segnities007.login.domain.usecase.PasswordRegistrationResult
import com.segnities007.login.domain.usecase.RegisterPasswordUseCase
import com.segnities007.login.domain.usecase.SaveBiometricSecretUseCase
import com.segnities007.login.presentation.contract.RegisterIntent
import com.segnities007.login.presentation.contract.RegisterNavigationRequest
import com.segnities007.login.presentation.contract.RegisterState
import com.segnities007.login.presentation.contract.RegisterUserMessage
import com.segnities007.login.presentation.operation.BiometricAuthenticateOperation
import com.segnities007.login.presentation.operation.BiometricAuthenticationResult
import com.segnities007.login.presentation.operation.BiometricEnrollmentPreparationResult
import com.segnities007.login.presentation.operation.BiometricEnrollmentRequest
import com.segnities007.login.presentation.operation.BiometricSecretEncryptionResult
import com.segnities007.login.presentation.operation.EncryptedBiometricSecret
import com.segnities007.login.presentation.operation.EncryptBiometricSecretOperation
import com.segnities007.login.presentation.operation.PrepareBiometricEnrollmentOperation
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 新規登録（マスターパスワード設定と任意の生体登録）を扱う。 */
internal class RegisterViewModel(
    private val registerPasswordUseCase: RegisterPasswordUseCase,
    private val prepareBiometricEnrollmentOperation: PrepareBiometricEnrollmentOperation,
    private val biometricAuthenticateOperation: BiometricAuthenticateOperation,
    private val encryptBiometricSecretOperation: EncryptBiometricSecretOperation,
    private val saveBiometricSecretUseCase: SaveBiometricSecretUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterState())

    /** 画面が購読する単一の UI 状態。 */
    val uiState = _uiState.asStateFlow()

    /** UI からの操作を 1 入口で処理する。 */
    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.SubmitRegistration -> register(
                passwordBytes = intent.passwordBytes,
                confirmPasswordBytes = intent.confirmPasswordBytes
            )
            RegisterIntent.ConsumeUserMessage -> consumeUserMessage()
            RegisterIntent.ConsumeNavigation -> consumeNavigation()
        }
    }

    private fun consumeUserMessage() {
        _uiState.update { it.copy(pendingUserMessage = null) }
    }

    private fun consumeNavigation() {
        _uiState.update { it.copy(pendingNavigation = null) }
    }

    private fun register(
        passwordBytes: ByteArray,
        confirmPasswordBytes: ByteArray
    ) {
        val validatedPasswordBytes = validatedPasswordOrNull(
            passwordBytes = passwordBytes,
            confirmPasswordBytes = confirmPasswordBytes
        ) ?: return
        submitRegistration(validatedPasswordBytes)
    }

    private fun submitRegistration(passwordBytes: ByteArray) {
        viewModelScope.launch {
            startLoading()
            try {
                val result = executePasswordRegistration(
                    String(passwordBytes, StandardCharsets.UTF_8)
                )
                reducePasswordRegistration(passwordBytes, result)
            } finally {
                passwordBytes.fill(0)
            }
        }
    }

    private suspend fun executePasswordRegistration(
        password: String
    ): PasswordRegistrationResult {
        return registerPasswordUseCase(password)
    }

    private suspend fun reducePasswordRegistration(
        passwordBytes: ByteArray,
        result: PasswordRegistrationResult
    ) {
        when (result) {
            PasswordRegistrationResult.Success -> continueBiometricEnrollment(passwordBytes)
            is PasswordRegistrationResult.TooShort -> handlePasswordTooShort(result.minimumLength)
            PasswordRegistrationResult.TooCommon -> handlePasswordTooCommon()
            PasswordRegistrationResult.UntrustedEnvironment -> handleUntrustedEnvironment()
            PasswordRegistrationResult.Failure -> handleRegistrationFailure()
        }
    }

    private suspend fun continueBiometricEnrollment(passwordBytes: ByteArray) {
        val request = executeBiometricEnrollmentPreparation() ?: return
        val authentication = executeBiometricEnrollmentAuthentication(request.cryptoObject) ?: return
        val secret = executeBiometricSecretEncryption(
            passwordBytes = passwordBytes,
            authenticationResult = authentication
        ) ?: return
        val result = executeBiometricSecretPersistence(secret)
        reduceBiometricSecretPersistence(result)
    }

    private suspend fun executeBiometricEnrollmentPreparation(): BiometricEnrollmentRequest? {
        return when (val result = prepareBiometricEnrollmentOperation()) {
            is BiometricEnrollmentPreparationResult.Ready -> result.request
            BiometricEnrollmentPreparationResult.Failure -> {
                navigateToLoginSkippingBiometric()
                null
            }
        }
    }

    private suspend fun executeBiometricEnrollmentAuthentication(
        cryptoObject: BiometricPrompt.CryptoObject
    ): BiometricPrompt.AuthenticationResult? {
        return when (val result = biometricAuthenticateOperation(cryptoObject)) {
            is BiometricAuthenticationResult.Authenticated -> result.result
            is BiometricAuthenticationResult.Failure -> {
                navigateToLoginSkippingBiometric()
                null
            }
        }
    }

    private suspend fun executeBiometricSecretEncryption(
        passwordBytes: ByteArray,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): EncryptedBiometricSecret? {
        return when (
            val result = encryptBiometricSecretOperation(
                password = passwordBytes,
                authenticationResult = authenticationResult
            )
        ) {
            is BiometricSecretEncryptionResult.Success -> result.secret
            BiometricSecretEncryptionResult.Failure -> {
                navigateToLoginSkippingBiometric()
                null
            }
        }
    }

    private suspend fun executeBiometricSecretPersistence(
        secret: EncryptedBiometricSecret
    ): BiometricSecretSaveResult {
        return saveBiometricSecretUseCase(
            encryptedSecret = secret.encryptedSecret,
            iv = secret.iv
        )
    }

    private suspend fun reduceBiometricSecretPersistence(
        result: BiometricSecretSaveResult
    ) {
        when (result) {
            BiometricSecretSaveResult.Success -> emitLoginNavigation(R.string.register_success_with_biometric)
            BiometricSecretSaveResult.UntrustedEnvironment -> handleUntrustedEnvironment()
            BiometricSecretSaveResult.Failure -> navigateToLoginSkippingBiometric()
        }
    }

    private suspend fun navigateToLoginSkippingBiometric() {
        emitLoginNavigation(R.string.register_success_without_biometric)
    }

    private fun validatedPasswordOrNull(
        passwordBytes: ByteArray,
        confirmPasswordBytes: ByteArray
    ): ByteArray? {
        if (passwordBytes.isEmpty()) {
            confirmPasswordBytes.fill(0)
            emitError(R.string.register_error_empty_password)
            return null
        }
        if (!passwordBytes.contentEquals(confirmPasswordBytes)) {
            passwordBytes.fill(0)
            confirmPasswordBytes.fill(0)
            emitError(R.string.register_error_passwords_do_not_match)
            return null
        }
        confirmPasswordBytes.fill(0)
        return passwordBytes
    }

    private suspend fun handleRegistrationFailure() {
        stopLoading()
        setPendingUserMessage(RegisterUserMessage.ToastRes(R.string.register_error_failed))
    }

    private suspend fun handlePasswordTooShort(minimumLength: Int) {
        stopLoading()
        setPendingUserMessage(
            RegisterUserMessage.ToastResArgs(
                messageRes = R.string.register_error_password_too_short,
                formatArgs = listOf(minimumLength)
            )
        )
    }

    private suspend fun handlePasswordTooCommon() {
        stopLoading()
        setPendingUserMessage(RegisterUserMessage.ToastRes(R.string.register_error_password_too_common))
    }

    private suspend fun handleUntrustedEnvironment() {
        stopLoading()
        setPendingUserMessage(RegisterUserMessage.ToastRes(R.string.register_error_untrusted_environment))
    }

    private fun startLoading() {
        setLoading(true)
    }

    private fun stopLoading() {
        setLoading(false)
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun setPendingUserMessage(message: RegisterUserMessage?) {
        _uiState.update { it.copy(pendingUserMessage = message) }
    }

    private suspend fun emitLoginNavigation(@StringRes messageRes: Int) {
        _uiState.update {
            it.copy(pendingNavigation = RegisterNavigationRequest.ToLogin(preludeToastRes = messageRes))
        }
    }

    private fun emitError(@StringRes messageRes: Int) {
        setPendingUserMessage(RegisterUserMessage.ToastRes(messageRes))
    }
}
