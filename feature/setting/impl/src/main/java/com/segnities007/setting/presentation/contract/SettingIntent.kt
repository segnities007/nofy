package com.segnities007.setting.presentation.contract

import com.segnities007.settings.IdleLockTimeoutOption
import com.segnities007.settings.ThemeMode

/**
 * 設定画面から [com.segnities007.setting.presentation.viewmodel.SettingViewModel] へ渡すユーザー操作・副作用要求。
 */
sealed interface SettingIntent {
    /** 表示中の設定タブ（見た目／セキュリティ／アプリ）を切り替える。 */
    data class SelectSection(val section: SettingsSection) : SettingIntent

    /** ライト／ダーク等のテーマを選ぶ。 */
    data class SelectThemeMode(val themeMode: ThemeMode) : SettingIntent

    /** 本文フォントのスケールを変更する。 */
    data class ChangeFontScale(val fontScale: Float) : SettingIntent

    /** フォアグラウンド無操作ロックまでの時間を変更する。 */
    data class SelectIdleLockTimeout(val option: IdleLockTimeoutOption) : SettingIntent

    /** マスターパスワード変更を試みる（確認一致・検証は VM 側）。 */
    data class SavePassword(
        val currentPassword: String,
        val newPassword: String,
        val confirmPassword: String
    ) : SettingIntent

    /** 保存済みの生体シークレットを削除し、生体ログインをオフにする。 */
    data object DisableBiometric : SettingIntent

    /** 生体登録用ダイアログを開く。 */
    data object OpenBiometricEnrollmentDialog : SettingIntent

    /** 生体登録ダイアログを閉じる。 */
    data object DismissBiometricEnrollmentDialog : SettingIntent

    /** 生体登録フロー実行中フラグ（プログレス・トグル無効化用）。 */
    data class SetBiometricEnrollmentBusy(val inProgress: Boolean) : SettingIntent

    /** マスター確認のうえアプリデータを初期化する。 */
    data class ResetApp(val currentPassword: String) : SettingIntent

    /** アプリをロックしログインへ戻す。 */
    data object Lock : SettingIntent

    /** 表示済みの [SettingUserMessage] をクリアする。 */
    data object ConsumeUserMessage : SettingIntent

    /** 処理済みの [SettingNavigationRequest] をクリアする。 */
    data object ConsumeNavigation : SettingIntent
}
