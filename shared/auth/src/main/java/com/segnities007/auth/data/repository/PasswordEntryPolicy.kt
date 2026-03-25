package com.segnities007.auth.data.repository

/** ローカルに永続化するパスワード試行回数とロックアウト終了時刻。 */
internal data class PasswordEntryState(
    val failedAttempts: Int = 0,
    val lockoutEndsAtMillis: Long = 0L
)

/** 段階的ロックアウト（5 回ごとに延長）の純粋ロジック。 */
internal object PasswordEntryPolicy {
    private const val AttemptsPerStage = 5
    private val lockoutDurationsMillis = longArrayOf(
        30_000L,
        60_000L,
        5 * 60_000L,
        15 * 60_000L
    )

    fun activeLockoutRemainingMillis(
        state: PasswordEntryState,
        nowMillis: Long
    ): Long? {
        val remaining = state.lockoutEndsAtMillis - nowMillis
        return remaining.takeIf { it > 0L }
    }

    fun applyFailure(
        state: PasswordEntryState,
        nowMillis: Long
    ): PasswordEntryState {
        val normalizedState = normalize(state, nowMillis)
        val failedAttempts = normalizedState.failedAttempts + 1
        val shouldLock = failedAttempts >= AttemptsPerStage
        if (!shouldLock) {
            return normalizedState.copy(failedAttempts = failedAttempts)
        }

        val stage = ((failedAttempts - AttemptsPerStage) / AttemptsPerStage)
            .coerceAtMost(lockoutDurationsMillis.lastIndex)
        return normalizedState.copy(
            failedAttempts = failedAttempts,
            lockoutEndsAtMillis = nowMillis + lockoutDurationsMillis[stage]
        )
    }

    fun cleared(): PasswordEntryState = PasswordEntryState()

    fun normalize(
        state: PasswordEntryState,
        nowMillis: Long
    ): PasswordEntryState {
        val remainingLockout = activeLockoutRemainingMillis(state, nowMillis)
        if (remainingLockout != null) {
            return state
        }

        if (state.lockoutEndsAtMillis == 0L) {
            return state
        }

        return state.copy(lockoutEndsAtMillis = 0L)
    }
}
