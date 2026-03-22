package com.segnities007.nofy.security

import com.segnities007.auth.domain.security.SensitiveOperationBlockedException
import com.segnities007.auth.domain.security.SensitiveOperationGuard

internal class CachedSensitiveOperationGuard(
    private val riskyEnvironmentDetector: RiskyEnvironmentDetector,
    private val nowMillisProvider: () -> Long = System::currentTimeMillis
) : SensitiveOperationGuard {
    @Volatile
    private var lastCheckedAtMillis: Long = 0L

    @Volatile
    private var wasBlocked: Boolean = false

    override fun ensureSensitiveOperationAllowed() {
        if (isBlocked()) {
            throw SensitiveOperationBlockedException()
        }
    }

    @Synchronized
    private fun isBlocked(): Boolean {
        val nowMillis = nowMillisProvider()
        val cacheAge = nowMillis - lastCheckedAtMillis
        val shouldRefresh = lastCheckedAtMillis == 0L ||
            nowMillis < lastCheckedAtMillis ||
            cacheAge >= CacheWindowMillis

        if (shouldRefresh) {
            wasBlocked = riskyEnvironmentDetector.detect() != null
            lastCheckedAtMillis = nowMillis
        }

        return wasBlocked
    }

    private companion object {
        const val CacheWindowMillis = 500L
    }
}
