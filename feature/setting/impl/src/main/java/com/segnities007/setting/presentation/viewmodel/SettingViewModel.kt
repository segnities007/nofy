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
import com.segnities007.setting.presentation.contract.SettingIntent
import com.segnities007.setting.presentation.contract.SettingNavigationRequest
import com.segnities007.setting.presentation.contract.SettingState
import com.segnities007.setting.presentation.contract.SettingUserMessage
import com.segnities007.setting.presentation.contract.SettingsSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 設定画面の [SettingState] を集約し、[SettingIntent] を処理する。 */
class SettingViewModel(
    private val authRepository: AuthRepository,
    private val uiSettingsRepository: UiSettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingState())

    /** 画面が購読する単一の UI 状態。 */
    val uiState = _uiState.asStateFlow()

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

    /** UI からの操作・副作用要求を 1 入口で処理する。 */
    fun onIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.SelectSection -> updateCurrentSection(intent.section)
            is SettingIntent.SelectThemeMode -> updateThemeMode(intent.themeMode)
            is SettingIntent.ChangeFontScale -> updateFontScale(intent.fontScale)
            is SettingIntent.SavePassword -> changePassword(
                currentPassword = intent.currentPassword,
                newPassword = intent.newPassword,
                confirmPassword = intent.confirmPassword
            )
            SettingIntent.DisableBiometric -> disableBiometric()
            SettingIntent.OpenBiometricEnrollmentDialog -> openBiometricEnrollmentDialog()
            SettingIntent.DismissBiometricEnrollmentDialog -> dismissBiometricEnrollmentDialog()
            is SettingIntent.SetBiometricEnrollmentBusy -> setBiometricEnrollmentBusy(intent.inProgress)
            is SettingIntent.ResetApp -> resetApp(intent.currentPassword)
            SettingIntent.Lock -> lock()
            SettingIntent.ConsumeUserMessage -> consumeUserMessage()
            SettingIntent.ConsumeNavigation -> consumeNavigation()
        }
    }

    private fun openBiometricEnrollmentDialog() {
        _uiState.update { it.copy(biometricEnrollmentDialogVisible = true) }
    }

    private fun dismissBiometricEnrollmentDialog() {
        _uiState.update { it.copy(biometricEnrollmentDialogVisible = false) }
    }

    private fun setBiometricEnrollmentBusy(inProgress: Boolean) {
        _uiState.update { it.copy(isBiometricEnrollmentInProgress = inProgress) }
    }

    private fun disableBiometric() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDisablingBiometric = true) }
            val result = authRepository.setBiometricEnabled(false)
            _uiState.update { it.copy(isDisablingBiometric = false) }
            val messageRes =
                if (result.isSuccess) {
                    R.string.settings_toast_biometric_updated
                } else {
                    R.string.settings_toast_biometric_update_failed
                }
            setPendingUserMessage(SettingUserMessage.ToastRes(messageRes))
        }
    }

    private fun consumeUserMessage() {
        _uiState.update { it.copy(pendingUserMessage = null) }
    }

    private fun consumeNavigation() {
        _uiState.update { it.copy(pendingNavigation = null) }
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

    private fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val request = validatedPasswordChangeOrNull(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        ) ?: return
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

    private fun validatedPasswordChangeOrNull(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): PasswordChangeRequest? {
        if (newPassword != confirmPassword) {
            notifyPasswordMismatch()
            return null
        }

        return PasswordChangeRequest(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }

    private fun validatedResetPasswordOrNull(currentPassword: String): String? {
        if (currentPassword.isBlank()) {
            setPendingUserMessage(SettingUserMessage.ToastRes(R.string.settings_toast_reset_password_required))
            return null
        }
        return currentPassword
    }

    private fun startPasswordChange() {
        setPasswordUpdating(true)
    }

    private suspend fun reducePasswordChange(result: Result<Unit>) {
        if (result.isFailure) {
            handlePasswordChangeFailure(result)
            return
        }
        handlePasswordChangeSuccess()
    }

    private suspend fun handlePasswordChangeSuccess() {
        _uiState.update {
            it.copy(
                isPasswordUpdating = false,
                passwordDraftClearNonce = it.passwordDraftClearNonce + 1
            )
        }
        setPendingUserMessage(SettingUserMessage.ToastRes(R.string.settings_toast_password_updated))
    }

    private suspend fun handlePasswordChangeFailure(result: Result<Unit>) {
        finishPasswordChange()
        setPendingUserMessage(SettingUserMessage.ToastRes(resolvePasswordChangeFailureMessage(result)))
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
        if (result.isFailure) {
            handleResetFailure(result)
            return
        }
        finishReset()
        setPendingNavigation(SettingNavigationRequest.ToSignUp)
    }

    private suspend fun handleResetFailure(result: Result<Unit>) {
        finishReset()
        setPendingUserMessage(SettingUserMessage.ToastRes(resolveResetFailureMessage(result)))
    }

    private fun finishReset() {
        setResetting(false)
    }

    private suspend fun reduceLock(result: Result<Unit>) {
        if (result.isFailure) {
            setPendingUserMessage(SettingUserMessage.ToastRes(R.string.settings_toast_lock_failed))
            return
        }
        setPendingNavigation(SettingNavigationRequest.ToLogin)
    }

    private fun notifyPasswordMismatch() {
        setPendingUserMessage(SettingUserMessage.ToastRes(R.string.settings_toast_passwords_do_not_match))
    }

    private fun resolvePasswordChangeFailureMessage(result: Result<Unit>): Int {
        val error = result.exceptionOrNull()
        if (error is AuthException.PasswordTooShort) return R.string.settings_toast_password_too_short
        if (error is AuthException.PasswordTooCommon) return R.string.settings_toast_password_too_common
        if (error is AuthException.InvalidPassword) return R.string.settings_toast_current_password_invalid
        if (error is AuthException.UntrustedEnvironment) return R.string.settings_toast_untrusted_environment
        if (error is AuthException.LockedOut) return R.string.settings_toast_too_many_attempts
        return R.string.settings_toast_password_update_failed
    }

    private fun resolveResetFailureMessage(result: Result<Unit>): Int {
        val error = result.exceptionOrNull()
        if (error is AuthException.InvalidPassword) return R.string.settings_toast_reset_invalid_password
        if (error is AuthException.UntrustedEnvironment) return R.string.settings_toast_untrusted_environment
        if (error is AuthException.LockedOut) return R.string.settings_toast_too_many_attempts
        return R.string.settings_toast_reset_failed
    }

    private fun updateCurrentSection(section: SettingsSection) {
        _uiState.update { it.copy(currentSection = section) }
    }

    private fun setPasswordUpdating(isUpdating: Boolean) {
        _uiState.update { it.copy(isPasswordUpdating = isUpdating) }
    }

    private fun setResetting(isResetting: Boolean) {
        _uiState.update { it.copy(isResetting = isResetting) }
    }

    private fun setPendingUserMessage(message: SettingUserMessage?) {
        _uiState.update { it.copy(pendingUserMessage = message) }
    }

    private fun setPendingNavigation(request: SettingNavigationRequest?) {
        _uiState.update { it.copy(pendingNavigation = request) }
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
