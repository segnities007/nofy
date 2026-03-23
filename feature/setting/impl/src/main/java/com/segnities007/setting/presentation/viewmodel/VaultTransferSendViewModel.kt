package com.segnities007.setting.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.localtransfer.LocalTransferKeyMaterial
import com.segnities007.localtransfer.LocalTransferQrPayload
import com.segnities007.localtransfer.LocalVaultTransfer
import com.segnities007.note.api.NoteVaultTransferPort
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 送信側ボルト転送画面の状態（エクスポート → QR スキャン → 送信）。 */
internal sealed class VaultSendUiState {
    /** 未開始または完了後に戻した初期状態。 */
    data object Idle : VaultSendUiState()

    /** パックファイルを書き出し中。 */
    data object Exporting : VaultSendUiState()

    /** 書き出し済み。ピアの QR を読み取り待ち。[packedFile] を送信に使う。 */
    data class Scanning(val packedFile: File) : VaultSendUiState()

    /** LAN 経由でファイル送信中。 */
    data object Sending : VaultSendUiState()

    /** 送信が正常終了した。 */
    data object Done : VaultSendUiState()

    /** ユーザー向けメッセージ付きの失敗。 */
    data class Failed(val message: String) : VaultSendUiState()
}

/** ボルトのエクスポートと、QR で確立したピアへの送信を orchestrate する。 */
internal class VaultTransferSendViewModel(
    application: Application,
    private val transferPort: NoteVaultTransferPort
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<VaultSendUiState>(VaultSendUiState.Idle)

    /** 送信フローの現在状態。 */
    val uiState: StateFlow<VaultSendUiState> = _uiState.asStateFlow()

    /** [password] で DB を開き、パックファイルを生成してスキャン待ちへ進める。 */
    fun beginExport(password: String) {
        if (password.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = VaultSendUiState.Exporting
            val r = transferPort.exportPackedVaultFile(password)
            r.fold(
                onSuccess = { file -> _uiState.value = VaultSendUiState.Scanning(file) },
                onFailure = { e ->
                    _uiState.value = VaultSendUiState.Failed(e.message ?: "Export failed")
                }
            )
        }
    }

    /** ピアから受け取った QR ペイロード（JSON）を解釈し、転送を実行する。 */
    fun onQrDecoded(raw: String) {
        val scanning = _uiState.value as? VaultSendUiState.Scanning ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = VaultSendUiState.Sending
            runCatching {
                val qr = LocalTransferQrPayload.parseJson(raw)
                val clientKey = LocalTransferKeyMaterial.generate()
                LocalVaultTransfer.sendToPeer(
                    qr = qr,
                    clientKey = clientKey,
                    vaultFile = scanning.packedFile,
                    timeoutMs = TRANSFER_TIMEOUT_MS
                ).getOrThrow()
            }.fold(
                onSuccess = {
                    scanning.packedFile.delete()
                    _uiState.value = VaultSendUiState.Done
                },
                onFailure = { e ->
                    scanning.packedFile.delete()
                    _uiState.value = VaultSendUiState.Failed(e.message ?: "Send failed")
                }
            )
        }
    }

    private companion object {
        const val TRANSFER_TIMEOUT_MS = 120_000L
    }
}
