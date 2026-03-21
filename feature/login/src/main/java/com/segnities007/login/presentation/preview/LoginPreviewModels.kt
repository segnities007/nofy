package com.segnities007.login.presentation.preview

import com.segnities007.login.presentation.contract.LoginState
import com.segnities007.login.presentation.contract.RegisterState

internal fun previewLoginState(): LoginState {
    return LoginState(
        password = "secure-password",
        isBiometricAvailable = true,
        isBiometricEnabled = true
    )
}

internal fun previewRegisterState(): RegisterState {
    return RegisterState(
        password = "secure-password",
        confirmPassword = "secure-password"
    )
}
