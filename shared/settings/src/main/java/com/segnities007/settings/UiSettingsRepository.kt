package com.segnities007.settings

import kotlinx.coroutines.flow.StateFlow

/**
 * テーマ・フォントスケールなど UI 設定の永続化と [StateFlow] による購読を提供する。
 */
interface UiSettingsRepository {
    /** 現在の UI 設定（テーマ・フォント倍率など）。 */
    val settings: StateFlow<UiSettings>

    /** ライト／ダーク等のテーマを永続化する。 */
    suspend fun setThemeMode(themeMode: ThemeMode)

    /** フォントスケールを永続化する。 */
    suspend fun setFontScale(fontScale: Float)

    /** フォアグラウンド無操作ロックまでの時間を永続化する。 */
    suspend fun setIdleLockTimeout(option: IdleLockTimeoutOption)

    /** UI 設定を既定値へ戻す。 */
    suspend fun reset()
}
