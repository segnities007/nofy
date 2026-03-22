package com.segnities007.setting.presentation.contract

import org.junit.Assert.assertTrue
import org.junit.Test

class SettingStateTest {

    @Test
    fun isAnyLoading_isTrueWhenAnyOperationIsRunning() {
        val state = SettingState(isResetting = true)

        assertTrue(state.isAnyLoading)
    }

    @Test
    fun isAnyLoading_isTrueWhenDisablingBiometric() {
        val state = SettingState(isDisablingBiometric = true)

        assertTrue(state.isAnyLoading)
    }

    @Test
    fun isBiometricSwitchBusy_isTrueWhenDisablingOrEnrollmentInProgress() {
        assertTrue(SettingState(isDisablingBiometric = true).isBiometricSwitchBusy)
        assertTrue(SettingState(isBiometricEnrollmentInProgress = true).isBiometricSwitchBusy)
    }
}
