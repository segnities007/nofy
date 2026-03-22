package com.segnities007.nofy.security

import com.segnities007.auth.domain.security.SensitiveOperationBlockedException
import com.segnities007.auth.domain.security.SensitiveOperationGuard

internal class SnapshotSensitiveOperationGuard(
    private val snapshotHolder: RiskyEnvironmentSnapshotHolder
) : SensitiveOperationGuard {
    override fun ensureSensitiveOperationAllowed() {
        if (snapshotHolder.currentRiskyEnvironment() != null) {
            throw SensitiveOperationBlockedException()
        }
    }
}
