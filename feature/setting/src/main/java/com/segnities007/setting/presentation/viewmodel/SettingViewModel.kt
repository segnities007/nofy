package com.segnities007.setting.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.auth.domain.error.AuthException
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.settings.ThemeMode
import com.segnities007.settings.UiSettings
import com.segnities007.settings.UiSettingsRepository
import com.segnities007.setting.R
import com.segnities007.setting.presentation.contract.SettingEffect
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingsSection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingViewModel(
    private val authRepository: AuthRepository,
    private val uiSettingsRepository: UiSettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                uiSettingsRepository.settings,
                authRepository.isBiometricEnabled()
            ) { uiSettings, biometric: Boolean ->
                ObservedSettings(
                    uiSettings = uiSettings,
                    isBiometricEnabled = biometric
                )
            }.collectLatest(::reduceObservedSettings)
        }
    }

    fun onIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.SelectSection -> updateCurrentSection(intent.section)
            is SettingIntent.SelectThemeMode -> updateThemeMode(intent.themeMode)
            is SettingIntent.ChangeFontScale -> updateFontScale(intent.fontScale)
            is SettingIntent.SetBiometricEnabled -> setBiometricEnabled(intent.enabled)
            is SettingIntent.ChangeCurrentPassword -> updateCurrentPassword(intent.value)
            is SettingIntent.ChangeNewPassword -> updateNewPassword(intent.value)
            is SettingIntent.ChangeConfirmPassword -> updateConfirmPassword(intent.value)
            SettingIntent.SavePassword -> changePassword()
            is SettingIntent.ResetApp -> resetApp(intent.currentPassword)
            SettingIntent.Lock -> lock()
        }
    }

    private fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            uiSettingsRepository.setThemeMode(themeMode)
        }
    }

    private fun updateFontScale(fontScale: Float) {
        viewModelScope.launch {
            uiSettingsRepository.setFontScale(fontScale)
        }
    }

    private fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            startBiometricUpdate()
            val result = authRepository.setBiometricEnabled(enabled)
            reduceBiometricUpdate(result)
        }
    }

    private fun changePassword() {
        val request = validatedPasswordChangeOrNull() ?: return
        submitPasswordChange(request)
    }

    private fun submitPasswordChange(request: PasswordChangeRequest) {
        viewModelScope.launch {
            startPasswordChange()
            val result = authRepository.changePassword(
                currentPassword = request.currentPassword,
                newPassword = request.newPassword
            )
            reducePasswordChange(result)
        }
    }

    private fun resetApp(currentPassword: String) {
        val password = validatedResetPasswordOrNull(currentPassword) ?: return
        submitReset(password)
    }

    private fun submitReset(currentPassword: String) {
        viewModelScope.launch {
            startReset()
            val result = executeReset(currentPassword)
            reduceReset(result)
        }
    }

    private fun lock() {
        viewModelScope.launch {
            val result = authRepository.lock()
            reduceLock(result)
        }
    }

    private fun reduceObservedSettings(observed: ObservedSettings) {
        _uiState.update {
            it.copy(
                themeMode = observed.uiSettings.themeMode,
                fontScale = observed.uiSettings.fontScale,
                isBiometricEnabled = observed.isBiometricEnabled
            )
        }
    }

    private fun validatedPasswordChangeOrNull(): PasswordChangeRequest? {
        val state = _uiState.value
        if (state.newPassword != state.confirmPassword) {
            notifyPasswordMismatch()
            return null
        }

        return PasswordChangeRequest(
            currentPassword = state.currentPassword,
            newPassword = state.newPassword
        )
    }

    private fun validatedResetPasswordOrNull(currentPassword: String): String? {
        if (currentPassword.isNotBlank()) {
            return currentPassword
        }

        viewModelScope.launch {
            emitToast(R.string.settings_toast_reset_password_required)
        }
        return null
    }

    private fun startBiometricUpdate() {
        setBiometricUpdating(true)
    }

    private suspend fun reduceBiometricUpdate(result: Result<Unit>) {
        finishBiometricUpdate()
        if (result.isSuccess) {
            emitToast(R.string.settings_toast_biometric_updated)
            return
        }

        emitToast(R.string.settings_toast_biometric_update_failed)
    }

    private fun finishBiometricUpdate() {
        setBiometricUpdating(false)
    }

    private fun startPasswordChange() {
        setPasswordUpdating(true)
    }

    private suspend fun reducePasswordChange(result: Result<Unit>) {
        if (result.isSuccess) {
            handlePasswordChangeSuccess()
            return
        }

        handlePasswordChangeFailure(result)
    }

    private suspend fun handlePasswordChangeSuccess() {
        _uiState.update {
            it.copy(
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                isPasswordUpdating = false
            )
        }
        emitToast(R.string.settings_toast_password_updated)
    }

    private suspend fun handlePasswordChangeFailure(result: Result<Unit>) {
        finishPasswordChange()
        emitToast(resolvePasswordChangeFailureMessage(result))
    }

    private fun finishPasswordChange() {
        setPasswordUpdating(false)
    }

    private fun startReset() {
        setResetting(true)
    }

    private suspend fun executeReset(currentPassword: String): Result<Unit> {
        val authResetResult = authRepository.reset(currentPassword)
        if (authResetResult.isFailure) {
            return authResetResult
        }

        return runCatching {
            uiSettingsRepository.reset()
        }
    }

    private suspend fun reduceReset(result: Result<Unit>) {
        if (result.isSuccess) {
            emitEffect(SettingEffect.NavigateToSignUp)
            return
        }

        handleResetFailure(result)
    }

    private suspend fun handleResetFailure(result: Result<Unit>) {
        finishReset()
        emitToast(resolveResetFailureMessage(result))
    }

    private fun finishReset() {
        setResetting(false)
    }

    private suspend fun reduceLock(result: Result<Unit>) {
        if (result.isSuccess) {
            emitEffect(SettingEffect.NavigateToLogin)
            return
        }

        emitToast(R.string.settings_toast_lock_failed)
    }

    private fun notifyPasswordMismatch() {
        viewModelScope.launch {
            emitToast(R.string.settings_toast_passwords_do_not_match)
        }
    }

    private fun resolvePasswordChangeFailureMessage(result: Result<Unit>): Int {
        return when (val error = result.exceptionOrNull()) {
            is AuthException.PasswordTooShort -> R.string.settings_toast_password_too_short
            AuthException.PasswordTooCommon -> R.string.settings_toast_password_too_common
            AuthException.InvalidPassword -> R.string.settings_toast_current_password_invalid
            is AuthException.LockedOut -> R.string.settings_toast_too_many_attempts
            else -> R.string.settings_toast_password_update_failed
        }
    }

    private fun resolveResetFailureMessage(result: Result<Unit>): Int {
        return when (val error = result.exceptionOrNull()) {
            AuthException.InvalidPassword -> R.string.settings_toast_reset_invalid_password
            is AuthException.LockedOut -> R.string.settings_toast_too_many_attempts
            else -> R.string.settings_toast_reset_failed
        }
    }

    private fun updateCurrentSection(section: SettingsSection) {
        _uiState.update { it.copy(currentSection = section) }
    }

    private fun updateCurrentPassword(value: String) {
        _uiState.update { it.copy(currentPassword = value) }
    }

    private fun updateNewPassword(value: String) {
        _uiState.update { it.copy(newPassword = value) }
    }

    private fun updateConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    private fun setBiometricUpdating(isUpdating: Boolean) {
        _uiState.update { it.copy(isBiometricUpdating = isUpdating) }
    }

    private fun setPasswordUpdating(isUpdating: Boolean) {
        _uiState.update { it.copy(isPasswordUpdating = isUpdating) }
    }

    private fun setResetting(isResetting: Boolean) {
        _uiState.update { it.copy(isResetting = isResetting) }
    }

    private suspend fun emitToast(@StringRes messageRes: Int) {
        emitEffect(SettingEffect.ShowToastRes(messageRes))
    }

    private suspend fun emitEffect(effect: SettingEffect) {
        _effect.emit(effect)
    }

    private data class PasswordChangeRequest(
        val currentPassword: String,
        val newPassword: String
    )

    private data class ObservedSettings(
        val uiSettings: UiSettings,
        val isBiometricEnabled: Boolean
    )
}
