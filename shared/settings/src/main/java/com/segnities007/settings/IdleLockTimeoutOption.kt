package com.segnities007.settings

/**
 * フォアグラウンド無操作時にロックまで待つ時間（[timeoutMillis]）。
 * 永続化は [storageValue] を用いる。
 */
enum class IdleLockTimeoutOption(
    val storageValue: String,
    val timeoutMillis: Long
) {
    ThirtySeconds("30", 30_000L),
    OneMinute("60", 60_000L),
    TwoMinutes("120", 120_000L),
    FiveMinutes("300", 300_000L);

    companion object {
        fun fromStorage(value: String?): IdleLockTimeoutOption {
            return entries.firstOrNull { it.storageValue == value } ?: OneMinute
        }
    }
}

/** 設定 UI 用の表示順（チップ並び）。 */
val IdleLockTimeoutPresets: List<IdleLockTimeoutOption> = IdleLockTimeoutOption.entries
