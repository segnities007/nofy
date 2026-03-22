package com.segnities007.designsystem.input

import android.view.MotionEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter

fun Modifier.consumeObscuredTouches(
    enabled: Boolean,
    onBlocked: (() -> Unit)? = null
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInteropFilter { motionEvent ->
        if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN && motionEvent.isObscured()) {
            onBlocked?.invoke()
            true
        } else {
            false
        }
    }
}

private fun MotionEvent.isObscured(): Boolean {
    val obscuredFlags = MotionEvent.FLAG_WINDOW_IS_OBSCURED or
        MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED
    return flags and obscuredFlags != 0
}
