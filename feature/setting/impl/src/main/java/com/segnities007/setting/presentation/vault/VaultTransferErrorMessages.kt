package com.segnities007.setting.presentation.vault

import android.app.Application
import com.segnities007.localtransfer.LocalTransferException
import com.segnities007.setting.R

/** 転送フローで UI に出す固定文言（例外メッセージの直接表示を避ける）。 */
internal fun Application.messageForVaultTransferFailure(throwable: Throwable): String {
    return when (throwable) {
        is LocalTransferException.HandshakeFailed -> getString(R.string.settings_vault_error_connection)
        is LocalTransferException.PairingFailed -> getString(R.string.settings_vault_error_pairing)
        is LocalTransferException.InvalidPayload -> getString(R.string.settings_vault_error_transfer_corrupt)
        is LocalTransferException.IoFailed -> getString(R.string.settings_vault_error_connection)
        is IllegalArgumentException -> getString(R.string.settings_vault_error_qr)
        else -> getString(R.string.settings_vault_error_generic)
    }
}
