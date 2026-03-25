package com.segnities007.nofy.security

import com.segnities007.auth.domain.security.SensitiveOperationBlockedException
import com.segnities007.auth.domain.security.SensitiveOperationGuard

/**
 * [RiskyEnvironmentSnapshotHolder] のスナップショットに基づき、危険環境下では
 * [SensitiveOperationBlockedException] を投げる [SensitiveOperationGuard] 実装。
 */
internal class SnapshotSensitiveOperationGuard(
    private val snapshotHolder: RiskyEnvironmentSnapshotHolder
) : SensitiveOperationGuard {
    override fun ensureSensitiveOperationAllowed() {
        if (snapshotHolder.currentRiskyEnvironment() != null) {
            throw SensitiveOperationBlockedException()
        }
    }
}
