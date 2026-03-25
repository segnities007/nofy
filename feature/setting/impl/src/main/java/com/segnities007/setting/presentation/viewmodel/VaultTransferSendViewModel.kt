package com.segnities007.setting.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.localtransfer.LocalTransferKeyMaterial
import com.segnities007.localtransfer.LocalTransferQrPayload
import com.segnities007.localtransfer.LocalVaultTransfer
import com.segnities007.localtransfer.normalizedVaultPairingCodeOrNull
import com.segnities007.note.api.NoteVaultTransferPort
import com.segnities007.setting.R
import com.segnities007.setting.presentation.vault.messageForVaultTransferFailure
import java.io.File
import java.nio.charset.StandardCharsets
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

    /** QR 読取済み。受信側画面のペアリングコード入力後に送信する。 */
    data class PendingPairing(val packedFile: File, val qr: LocalTransferQrPayload) : VaultSendUiState()

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

    private val _pairingValidationError = MutableStateFlow<String?>(null)

    /** ペアリングコード桁数などのローカル検証エラー（[PendingPairing] 中のみ）。 */
    val pairingValidationError: StateFlow<String?> = _pairingValidationError.asStateFlow()

    /** [password] で DB を開き、パックファイルを生成してスキャン待ちへ進める。 */
    fun beginExport(password: String) {
        if (password.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _pairingValidationError.value = null
            _uiState.value = VaultSendUiState.Exporting
            val r = transferPort.exportPackedVaultFile(password)
            val app = getApplication<Application>()
            r.fold(
                onSuccess = { file -> _uiState.value = VaultSendUiState.Scanning(file) },
                onFailure = {
                    _uiState.value = VaultSendUiState.Failed(
                        app.getString(R.string.settings_vault_error_export)
                    )
                }
            )
        }
    }

    /** ピアから受け取った QR ペイロード（JSON）を解釈し、ペアリングコード入力へ進む。 */
    fun onQrDecoded(raw: String) {
        val scanning = _uiState.value as? VaultSendUiState.Scanning ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            runCatching { LocalTransferQrPayload.parseJson(raw) }.fold(
                onSuccess = { qr ->
                    _pairingValidationError.value = null
                    _uiState.value = VaultSendUiState.PendingPairing(scanning.packedFile, qr)
                },
                onFailure = { e ->
                    _uiState.value = VaultSendUiState.Failed(app.messageForVaultTransferFailure(e))
                }
            )
        }
    }

    /** 受信側に表示されたペアリングコードで送信を実行する。 */
    fun confirmSendWithPairing(rawPairingInput: String) {
        val pending = _uiState.value as? VaultSendUiState.PendingPairing ?: return
        val app = getApplication<Application>()
        val normalized = normalizedVaultPairingCodeOrNull(rawPairingInput)
        if (normalized == null) {
            _pairingValidationError.value =
                app.getString(R.string.settings_vault_error_pairing_format)
            return
        }
        _pairingValidationError.value = null
        val pairingUtf8 = normalized.toByteArray(StandardCharsets.UTF_8)
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = VaultSendUiState.Sending
            runCatching {
                val clientKey = LocalTransferKeyMaterial.generate()
                LocalVaultTransfer.sendToPeer(
                    qr = pending.qr,
                    clientKey = clientKey,
                    vaultFile = pending.packedFile,
                    timeoutMs = TRANSFER_TIMEOUT_MS,
                    pairingCodeUtf8 = pairingUtf8
                ).getOrThrow()
            }.fold(
                onSuccess = {
                    pending.packedFile.delete()
                    _uiState.value = VaultSendUiState.Done
                },
                onFailure = { e ->
                    pending.packedFile.delete()
                    _uiState.value = VaultSendUiState.Failed(app.messageForVaultTransferFailure(e))
                }
            )
        }
    }

    private companion object {
        const val TRANSFER_TIMEOUT_MS = 120_000L
    }
}
