package com.segnities007.auth.domain.error

/** 認証ドメインで想定される失敗（パスワードポリシー・ロックアウト・環境など）。 */
sealed class AuthException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause) {
    /** 既にマスターパスワードが登録済み。 */
    data object AlreadyRegistered : AuthException("Already registered")

    /** 未登録のためロック解除できない。 */
    data object NotRegistered : AuthException("Not registered")

    /** パスワードが一致しない。 */
    data object InvalidPassword : AuthException("Invalid password")

    /** 生体が未登録または利用不可。 */
    data object BiometricNotEnrolled : AuthException("Biometric not enrolled")

    /** 危険環境検出により操作が拒否された。 */
    data object UntrustedEnvironment : AuthException("Untrusted environment")

    /** 試行制限中。UI は [remainingMillis] を表示する。 */
    data class LockedOut(val remainingMillis: Long) : AuthException("Locked out")

    /** [minimumLength] 未満のパスワード。 */
    data class PasswordTooShort(val minimumLength: Int) : AuthException("Password too short")

    /** ポリシー上禁止されている単純パスワード。 */
    data object PasswordTooCommon : AuthException("Password too common")
}
