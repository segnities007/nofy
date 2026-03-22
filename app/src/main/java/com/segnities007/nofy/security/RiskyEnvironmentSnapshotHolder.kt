package com.segnities007.nofy.security

/**
 * [RiskyEnvironmentDetector] の結果を UI と [com.segnities007.auth.domain.security.SensitiveOperationGuard]
 * で共有する。キャッシュの二重管理や TTL ずれを避ける。
 */
internal class RiskyEnvironmentSnapshotHolder {
    @Volatile
    private var environment: RiskyEnvironment? = null

    fun publish(environment: RiskyEnvironment?) {
        this.environment = environment
    }

    fun currentRiskyEnvironment(): RiskyEnvironment? = environment
}
