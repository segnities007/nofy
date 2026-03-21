package com.segnities007.setting.presentation.contract

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingStateTest {

    @Test
    fun isAnyLoading_isTrueWhenAnyOperationIsRunning() {
        val state = SettingState(isResetting = true)

        assertTrue(state.isAnyLoading)
    }

    @Test
    fun canUpdatePassword_isFalseWhilePasswordUpdateIsRunning() {
        val state = SettingState(
            currentPassword = "old",
            newPassword = "new",
            confirmPassword = "new",
            isPasswordUpdating = true
        )

        assertFalse(state.canUpdatePassword)
    }
}
