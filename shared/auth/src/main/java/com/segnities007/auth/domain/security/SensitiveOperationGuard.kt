package com.segnities007.auth.domain.security

/**
 * 端末環境が信頼できないときに機密操作（認証・DB アクセス等）をブロックするためのガード。
 */
interface SensitiveOperationGuard {
    /**
     * 機密操作が許可される環境でない場合は [SensitiveOperationBlockedException] を投げる。
     */
    fun ensureSensitiveOperationAllowed()
}

/** [SensitiveOperationGuard] が危険環境を検知したときに投げる例外。 */
class SensitiveOperationBlockedException : IllegalStateException(
    "Sensitive operation blocked in an untrusted environment"
)
