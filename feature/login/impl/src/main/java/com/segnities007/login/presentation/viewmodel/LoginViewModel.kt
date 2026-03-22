package com.segnities007.login.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.login.R
import com.segnities007.login.domain.usecase.ClearBiometricSecretUseCase
import com.segnities007.login.domain.usecase.LoginSubmissionResult
import com.segnities007.login.domain.usecase.ObserveBiometricEnabledUseCase
import com.segnities007.login.domain.usecase.UnlockWithPasswordUseCase
import com.segnities007.login.presentation.contract.LoginIntent
import com.segnities007.login.presentation.contract.LoginNavigationRequest
import com.segnities007.login.presentation.contract.LoginState
import com.segnities007.login.presentation.contract.LoginUserMessage
import com.segnities007.login.presentation.operation.BiometricAuthenticateOperation
import com.segnities007.login.presentation.operation.BiometricAuthenticationResult
import com.segnities007.login.presentation.operation.BiometricPasswordDecryptionResult
import com.segnities007.login.presentation.operation.BiometricUnlockPreparationResult
import com.segnities007.login.presentation.operation.BiometricUnlockRequest
import com.segnities007.login.presentation.operation.DecryptBiometricPasswordOperation
import com.segnities007.login.presentation.operation.PrepareBiometricUnlockOperation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val unlockWithPasswordUseCase: UnlockWithPasswordUseCase,
    private val observeBiometricEnabledUseCase: ObserveBiometricEnabledUseCase,
    private val clearBiometricSecretUseCase: ClearBiometricSecretUseCase,
    private val prepareBiometricUnlockOperation: PrepareBiometricUnlockOperation,
    private val biometricAuthenticateOperation: BiometricAuthenticateOperation,
    private val decryptBiometricPasswordOperation: DecryptBiometricPasswordOperation
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    init {
        observeBiometricEnabled()
    }

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SubmitPassword -> unlock(intent.password)
            LoginIntent.BiometricLogin -> startBiometricUnlock()
            is LoginIntent.SetBiometricAvailability -> updateBiometricAvailability(intent.isAvailable)
            LoginIntent.ConsumeUserMessage -> consumeUserMessage()
            LoginIntent.ConsumeNavigation -> consumeNavigation()
        }
    }

    private fun consumeUserMessage() {
        _uiState.update { it.copy(pendingUserMessage = null) }
    }

    private fun consumeNavigation() {
        _uiState.update { it.copy(pendingNavigation = null) }
    }

    private fun unlock(password: String) {
        if (password.isEmpty()) {
            emitError(R.string.login_error_empty_password)
            return
        }
        submitPasswordUnlock(password)
    }

    private fun submitPasswordUnlock(password: String) {
        viewModelScope.launch {
            startLoading()
            val result = executePasswordUnlock(password)
            reduceSubmissionResult(result, R.string.login_error_unlock_failed)
        }
    }

    private suspend fun executePasswordUnlock(password: String): LoginSubmissionResult {
        return unlockWithPasswordUseCase(password)
    }

    private fun startBiometricUnlock() {
        viewModelScope.launch {
            val request = executeBiometricUnlockPreparation() ?: return@launch
            val authentication = executeBiometricAuthentication(request.cryptoObject) ?: return@launch
            submitBiometricUnlock(request, authentication)
        }
    }

    private suspend fun executeBiometricUnlockPreparation(): BiometricUnlockRequest? {
        return when (val result = prepareBiometricUnlockOperation()) {
            BiometricUnlockPreparationResult.MissingSecret -> {
                emitToast(R.string.login_error_biometric_not_set)
                null
            }

            BiometricUnlockPreparationResult.CredentialUnavailable -> {
                disableUnavailableBiometricLogin()
                null
            }

            BiometricUnlockPreparationResult.UntrustedEnvironment -> {
                emitToast(R.string.login_error_untrusted_environment)
                null
            }

            is BiometricUnlockPreparationResult.Ready -> result.request
        }
    }

    private suspend fun executeBiometricAuthentication(
        cryptoObject: BiometricPrompt.CryptoObject
    ): BiometricPrompt.AuthenticationResult? {
        return when (val result = biometricAuthenticateOperation(cryptoObject)) {
            is BiometricAuthenticationResult.Authenticated -> result.result
            is BiometricAuthenticationResult.Failure -> {
                setPendingUserMessage(LoginUserMessage.ToastString(result.message))
                null
            }
        }
    }

    private suspend fun executeBiometricUnlock(
        request: BiometricUnlockRequest,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): LoginSubmissionResult? {
        return when (
            val result = decryptBiometricPasswordOperation(
                request = request,
                authenticationResult = authenticationResult
            )
        ) {
            is BiometricPasswordDecryptionResult.Success -> result.submissionResult
            BiometricPasswordDecryptionResult.CredentialUnavailable -> {
                disableUnavailableBiometricLogin()
                null
            }

            BiometricPasswordDecryptionResult.Failure -> {
                emitToast(R.string.login_error_biometric_unlock_failed)
                null
            }
        }
    }

    private fun submitBiometricUnlock(
        request: BiometricUnlockRequest,
        authenticationResult: BiometricPrompt.AuthenticationResult
    ) {
        viewModelScope.launch {
            startLoading()
            val result = executeBiometricUnlock(request, authenticationResult)
            if (result == null) {
                stopLoading()
                return@launch
            }
            reduceSubmissionResult(result, R.string.login_error_biometric_unlock_failed)
        }
    }

    private suspend fun reduceSubmissionResult(
        result: LoginSubmissionResult,
        @StringRes failureMessageRes: Int
    ) {
        stopLoading()
        when (result) {
            LoginSubmissionResult.Success -> setPendingNavigation(LoginNavigationRequest.ToNotes)
            LoginSubmissionResult.UntrustedEnvironment -> emitToast(R.string.login_error_untrusted_environment)
            LoginSubmissionResult.Failure -> emitToast(failureMessageRes)
            is LoginSubmissionResult.LockedOut -> emitLockout(result.remainingMillis)
        }
    }

    private fun startLoading() {
        setLoading(true)
    }

    private fun stopLoading() {
        setLoading(false)
    }

    private fun updateBiometricAvailability(isAvailable: Boolean) {
        _uiState.update { it.copy(isBiometricAvailable = isAvailable) }
    }

    private fun updateBiometricEnabled(isEnabled: Boolean) {
        _uiState.update { it.copy(isBiometricEnabled = isEnabled) }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun setPendingUserMessage(message: LoginUserMessage?) {
        _uiState.update { it.copy(pendingUserMessage = message) }
    }

    private fun setPendingNavigation(request: LoginNavigationRequest?) {
        _uiState.update { it.copy(pendingNavigation = request) }
    }

    private suspend fun emitToast(@StringRes messageRes: Int) {
        setPendingUserMessage(LoginUserMessage.ToastRes(messageRes))
    }

    private suspend fun emitLockout(remainingMillis: Long) {
        setPendingUserMessage(LoginUserMessage.Lockout(remainingMillis))
    }

    private suspend fun disableUnavailableBiometricLogin() {
        clearBiometricSecretUseCase()
        emitToast(R.string.login_error_biometric_reenroll_required)
    }

    private fun emitError(@StringRes messageRes: Int) {
        setPendingUserMessage(LoginUserMessage.ToastRes(messageRes))
    }

    private fun observeBiometricEnabled() {
        viewModelScope.launch {
            observeBiometricEnabledUseCase().collect(::updateBiometricEnabled)
        }
    }
}
