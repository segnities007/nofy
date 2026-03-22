package com.segnities007.auth.domain.security

interface SensitiveOperationGuard {
    fun ensureSensitiveOperationAllowed()
}

class SensitiveOperationBlockedException : IllegalStateException(
    "Sensitive operation blocked in an untrusted environment"
)
