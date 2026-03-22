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
import com.segnities007.login.presentation.contract.RegisterEffect
import com.segnities007.login.presentation.contract.RegisterIntent
import com.segnities007.login.presentation.contract.RegisterState
import com.segnities007.login.presentation.operation.AuthenticateWithCryptoOperation
import com.segnities007.login.presentation.operation.BiometricAuthenticationResult
import com.segnities007.login.presentation.operation.BiometricEnrollmentPreparationResult
import com.segnities007.login.presentation.operation.BiometricEnrollmentRequest
import com.segnities007.login.presentation.operation.BiometricSecretEncryptionResult
import com.segnities007.login.presentation.operation.EncryptedBiometricSecret
import com.segnities007.login.presentation.operation.EncryptBiometricSecretOperation
import com.segnities007.login.presentation.operation.PrepareBiometricEnrollmentOperation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RegisterViewModel(
    private val registerPasswordUseCase: RegisterPasswordUseCase,
    private val prepareBiometricEnrollmentOperation: PrepareBiometricEnrollmentOperation,
    private val authenticateWithCryptoOperation: AuthenticateWithCryptoOperation,
    private val encryptBiometricSecretOperation: EncryptBiometricSecretOperation,
    private val saveBiometricSecretUseCase: SaveBiometricSecretUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RegisterEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.SubmitRegistration -> register(
                password = intent.password,
                confirmPassword = intent.confirmPassword
            )
        }
    }

    private fun register(
        password: String,
        confirmPassword: String
    ) {
        val validatedPassword = validatedPasswordOrNull(
            password = password,
            confirmPassword = confirmPassword
        ) ?: return
        submitRegistration(validatedPassword)
    }

    private fun submitRegistration(password: String) {
        viewModelScope.launch {
            startLoading()
            val result = executePasswordRegistration(password)
            reducePasswordRegistration(password, result)
        }
    }

    private suspend fun executePasswordRegistration(
        password: String
    ): PasswordRegistrationResult {
        return registerPasswordUseCase(password)
    }

    private suspend fun reducePasswordRegistration(
        password: String,
        result: PasswordRegistrationResult
    ) {
        when (result) {
            PasswordRegistrationResult.Success -> continueBiometricEnrollment(password)
            is PasswordRegistrationResult.TooShort -> handlePasswordTooShort(result.minimumLength)
            PasswordRegistrationResult.TooCommon -> handlePasswordTooCommon()
            PasswordRegistrationResult.UntrustedEnvironment -> handleUntrustedEnvironment()
            PasswordRegistrationResult.Failure -> handleRegistrationFailure()
        }
    }

    private suspend fun continueBiometricEnrollment(password: String) {
        val request = executeBiometricEnrollmentPreparation() ?: return
        val authentication = executeBiometricEnrollmentAuthentication(request.cryptoObject) ?: return
        val secret = executeBiometricSecretEncryption(
            password = password,
            authenticationResult = authentication
        ) ?: return
        val result = executeBiometricSecretPersistence(secret)
        reduceBiometricSecretPersistence(result)
    }

    private suspend fun executeBiometricEnrollmentPreparation(): BiometricEnrollmentRequest? {
        return when (val result = prepareBiometricEnrollmentOperation()) {
            is BiometricEnrollmentPreparationResult.Ready -> result.request
            BiometricEnrollmentPreparationResult.Failure -> {
                emitLoginNavigation(R.string.register_success_without_biometric)
                null
            }
        }
    }

    private suspend fun executeBiometricEnrollmentAuthentication(
        cryptoObject: BiometricPrompt.CryptoObject
    ): BiometricPrompt.AuthenticationResult? {
        return when (val result = authenticateWithCryptoOperation(cryptoObject)) {
            is BiometricAuthenticationResult.Authenticated -> result.result
            is BiometricAuthenticationResult.Failure -> {
                emitLoginNavigation(R.string.register_success_without_biometric)
                null
            }
        }
    }

    private suspend fun executeBiometricSecretEncryption(
        password: String,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): EncryptedBiometricSecret? {
        return when (
            val result = encryptBiometricSecretOperation(
                password = password,
                authenticationResult = authenticationResult
            )
        ) {
            is BiometricSecretEncryptionResult.Success -> result.secret
            BiometricSecretEncryptionResult.Failure -> {
                emitLoginNavigation(R.string.register_success_without_biometric)
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
            BiometricSecretSaveResult.Failure -> emitLoginNavigation(R.string.register_success_without_biometric)
        }
    }

    private fun validatedPasswordOrNull(
        password: String,
        confirmPassword: String
    ): String? {
        if (password.isEmpty()) {
            emitError(R.string.register_error_empty_password)
            return null
        }
        if (password != confirmPassword) {
            emitError(R.string.register_error_passwords_do_not_match)
            return null
        }
        return password
    }

    private suspend fun handleRegistrationFailure() {
        stopLoading()
        emitEffect(RegisterEffect.ShowToastRes(R.string.register_error_failed))
    }

    private suspend fun handlePasswordTooShort(minimumLength: Int) {
        stopLoading()
        emitEffect(RegisterEffect.ShowToastResArgs(
            messageRes = R.string.register_error_password_too_short,
            formatArgs = listOf(minimumLength)
        ))
    }

    private suspend fun handlePasswordTooCommon() {
        stopLoading()
        emitEffect(RegisterEffect.ShowToastRes(R.string.register_error_password_too_common))
    }

    private suspend fun handleUntrustedEnvironment() {
        stopLoading()
        emitEffect(RegisterEffect.ShowToastRes(R.string.register_error_untrusted_environment))
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

    private suspend fun emitLoginNavigation(@StringRes messageRes: Int) {
        emitEffect(RegisterEffect.NavigateToLogin(messageRes))
    }

    private suspend fun emitEffect(effect: RegisterEffect) {
        _effect.emit(effect)
    }

    private fun emitError(@StringRes messageRes: Int) {
        viewModelScope.launch {
            emitEffect(RegisterEffect.ShowToastRes(messageRes))
        }
    }
}
