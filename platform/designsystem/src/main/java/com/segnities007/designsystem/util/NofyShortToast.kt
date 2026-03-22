package com.segnities007.designsystem.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showShortToast(@StringRes messageRes: Int) {
    Toast.makeText(this, getString(messageRes), Toast.LENGTH_SHORT).show()
}

fun Context.showShortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showShortToast(@StringRes messageRes: Int, formatArgs: List<Any>) {
    Toast.makeText(
        this,
        getString(messageRes, *formatArgs.toTypedArray()),
        Toast.LENGTH_SHORT
    ).show()
}
