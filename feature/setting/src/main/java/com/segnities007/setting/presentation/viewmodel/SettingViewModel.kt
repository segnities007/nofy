package com.segnities007.setting.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            SettingIntent.ResetApp -> resetApp()
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

    private fun resetApp() {
        viewModelScope.launch {
            startReset()
            val result = authRepository.reset()
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

        handlePasswordChangeFailure()
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

    private suspend fun handlePasswordChangeFailure() {
        finishPasswordChange()
        emitToast(R.string.settings_toast_password_update_failed)
    }

    private fun finishPasswordChange() {
        setPasswordUpdating(false)
    }

    private fun startReset() {
        setResetting(true)
    }

    private suspend fun reduceReset(result: Result<Unit>) {
        if (result.isSuccess) {
            emitEffect(SettingEffect.NavigateToSignUp)
            return
        }

        handleResetFailure()
    }

    private suspend fun handleResetFailure() {
        finishReset()
        emitToast(R.string.settings_toast_reset_failed)
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
