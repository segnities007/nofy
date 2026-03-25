package com.segnities007.setting.presentation.contract

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordChangeDraftTest {

    @Test
    fun canSubmit_falseWhenUpdating() {
        val draft = PasswordChangeDraft(current = "a", new = "b", confirm = "b")

        assertFalse(draft.canSubmit(isPasswordUpdating = true))
    }

    @Test
    fun canSubmit_falseWhenAnyFieldBlank() {
        assertFalse(PasswordChangeDraft(current = "", new = "b", confirm = "b").canSubmit(false))
        assertFalse(PasswordChangeDraft(current = "a", new = "", confirm = "b").canSubmit(false))
        assertFalse(PasswordChangeDraft(current = "a", new = "b", confirm = "").canSubmit(false))
    }

    @Test
    fun canSubmit_trueWhenAllFilledAndNotUpdating() {
        val draft = PasswordChangeDraft(current = "a", new = "b", confirm = "b")

        assertTrue(draft.canSubmit(isPasswordUpdating = false))
    }
}
