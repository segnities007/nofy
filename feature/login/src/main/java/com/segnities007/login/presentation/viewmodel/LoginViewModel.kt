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
import com.segnities007.login.presentation.contract.LoginEffect
import com.segnities007.login.presentation.contract.LoginIntent
import com.segnities007.login.presentation.contract.LoginState
import com.segnities007.login.presentation.operation.AuthenticateWithCryptoOperation
import com.segnities007.login.presentation.operation.BiometricAuthenticationResult
import com.segnities007.login.presentation.operation.BiometricPasswordDecryptionResult
import com.segnities007.login.presentation.operation.BiometricUnlockPreparationResult
import com.segnities007.login.presentation.operation.BiometricUnlockRequest
import com.segnities007.login.presentation.operation.DecryptBiometricPasswordOperation
import com.segnities007.login.presentation.operation.PrepareBiometricUnlockOperation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val unlockWithPasswordUseCase: UnlockWithPasswordUseCase,
    private val observeBiometricEnabledUseCase: ObserveBiometricEnabledUseCase,
    private val clearBiometricSecretUseCase: ClearBiometricSecretUseCase,
    private val prepareBiometricUnlockOperation: PrepareBiometricUnlockOperation,
    private val authenticateWithCryptoOperation: AuthenticateWithCryptoOperation,
    private val decryptBiometricPasswordOperation: DecryptBiometricPasswordOperation
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        observeBiometricEnabled()
    }

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SubmitPassword -> unlock(intent.password)
            LoginIntent.BiometricLogin -> startBiometricUnlock()
            is LoginIntent.SetBiometricAvailability -> updateBiometricAvailability(intent.isAvailable)
        }
    }

    private fun unlock(password: String) {
        val validatedPassword = password.takeIf(String::isNotEmpty)
        if (validatedPassword != null) {
            submitPasswordUnlock(validatedPassword)
            return
        }

        emitError(R.string.login_error_empty_password)
    }

    private fun submitPasswordUnlock(password: String) {
        viewModelScope.launch {
            startLoading()
            val result = executePasswordUnlock(password)
            reducePasswordUnlock(result)
        }
    }

    private suspend fun executePasswordUnlock(password: String): LoginSubmissionResult {
        return unlockWithPasswordUseCase(password)
    }

    private suspend fun reducePasswordUnlock(result: LoginSubmissionResult) {
        stopLoading()
        when (result) {
            LoginSubmissionResult.Success -> emitEffect(LoginEffect.NavigateToNotes)
            LoginSubmissionResult.UntrustedEnvironment -> emitToast(R.string.login_error_untrusted_environment)
            LoginSubmissionResult.Failure -> emitToast(R.string.login_error_unlock_failed)
            is LoginSubmissionResult.LockedOut -> emitLockout(result.remainingMillis)
        }
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
        return when (val result = authenticateWithCryptoOperation(cryptoObject)) {
            is BiometricAuthenticationResult.Authenticated -> result.result
            is BiometricAuthenticationResult.Failure -> {
                emitEffect(LoginEffect.ShowToastMessage(result.message))
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
            reduceBiometricUnlock(result)
        }
    }

    private suspend fun reduceBiometricUnlock(result: LoginSubmissionResult) {
        stopLoading()
        when (result) {
            LoginSubmissionResult.Success -> emitEffect(LoginEffect.NavigateToNotes)
            LoginSubmissionResult.UntrustedEnvironment -> emitToast(R.string.login_error_untrusted_environment)
            LoginSubmissionResult.Failure -> emitToast(R.string.login_error_biometric_unlock_failed)
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

    private suspend fun emitToast(@StringRes messageRes: Int) {
        emitEffect(LoginEffect.ShowToastRes(messageRes))
    }

    private suspend fun emitEffect(effect: LoginEffect) {
        _effect.emit(effect)
    }

    private suspend fun emitLockout(remainingMillis: Long) {
        emitEffect(LoginEffect.ShowLockout(remainingMillis))
    }

    private suspend fun disableUnavailableBiometricLogin() {
        clearBiometricSecretUseCase()
        emitToast(R.string.login_error_biometric_reenroll_required)
    }

    private fun emitError(@StringRes messageRes: Int) {
        viewModelScope.launch {
            emitToast(messageRes)
        }
    }

    private fun observeBiometricEnabled() {
        viewModelScope.launch {
            observeBiometricEnabledUseCase().collect(::updateBiometricEnabled)
        }
    }
}
