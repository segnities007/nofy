package com.segnities007.setting.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 設定フローおよびボルト送受信サブ画面のナビゲーションキー。
 */
@Serializable
sealed interface SettingsRoute : NavKey {
    @Serializable
    data object Settings : SettingsRoute

    @Serializable
    data object VaultTransferSend : SettingsRoute

    @Serializable
    data object VaultTransferReceive : SettingsRoute
}
