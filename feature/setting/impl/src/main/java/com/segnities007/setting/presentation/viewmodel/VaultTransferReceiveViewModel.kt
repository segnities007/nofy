package com.segnities007.setting.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.localtransfer.LocalTransferKeyMaterial
import com.segnities007.localtransfer.LocalTransferQrPayload
import com.segnities007.localtransfer.SESSION_VERIFIER_BYTES
import com.segnities007.localtransfer.PAIRING_CODE_DIGIT_COUNT
import com.segnities007.localtransfer.LocalVaultTransfer
import com.segnities007.localtransfer.preferredLocalIpv4Address
import com.segnities007.note.api.NoteVaultTransferPort
import com.segnities007.setting.R
import com.segnities007.setting.presentation.vault.QrBitmapGenerator
import com.segnities007.setting.presentation.vault.messageForVaultTransferFailure
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Locale
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 受信側ボルト転送の状態（待受 QR → ファイル受信 → パスワードで取込）。 */
internal sealed class VaultReceiveUiState {
    /** セッション開始前、または再実行直後。 */
    data object Starting : VaultReceiveUiState()

    /**
     * ローカルで待受中。表示用の [qrBitmap] と、バックグラウンドで受信処理が進む。
     */
    data class Listening(
        val qrBitmap: Bitmap,
        /** QR に含めない 8 桁。送信側が手入力する。 */
        val pairingCode: String
    ) : VaultReceiveUiState()

    /** パックファイルの受信完了。ユーザー入力のボルトパスワードで取り込む。 */
    data class AwaitingPassword(val packedFile: File) : VaultReceiveUiState()

    /** [NoteVaultTransferPort.importPackedVaultFile] 実行中。 */
    data object Importing : VaultReceiveUiState()

    /** 取り込み成功。 */
    data object Done : VaultReceiveUiState()

    /** ユーザー向けメッセージ付きの失敗。 */
    data class Failed(val message: String) : VaultReceiveUiState()
}

/** ソケット待受・QR 表示・受信ファイルのインポートを orchestrate する。 */
internal class VaultTransferReceiveViewModel(
    application: Application,
    private val transferPort: NoteVaultTransferPort
) : AndroidViewModel(application) {

    private var serverSocket: ServerSocket? = null

    private val _uiState = MutableStateFlow<VaultReceiveUiState>(VaultReceiveUiState.Starting)

    /** 受信フローの現在状態。 */
    val uiState: StateFlow<VaultReceiveUiState> = _uiState.asStateFlow()

    /** 待受ソケットと QR を用意し、1 回だけピアからパックを受信する。 */
    fun startSession() {
        if (_uiState.value !is VaultReceiveUiState.Starting) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ip = preferredLocalIpv4Address()
                if (ip == null) {
                    _uiState.value = VaultReceiveUiState.Failed(
                        getApplication<Application>().getString(R.string.settings_vault_error_no_ipv4)
                    )
                    return@launch
                }
                val bindAddress = InetAddress.getByName(ip)
                val ss = ServerSocket()
                ss.reuseAddress = true
                ss.bind(InetSocketAddress(bindAddress, 0))
                serverSocket = ss
                val serverKey = LocalTransferKeyMaterial.generate()
                val sessionVerifier = ByteArray(SESSION_VERIFIER_BYTES).also {
                    SecureRandom().nextBytes(it)
                }
                val pairingNumeric = SecureRandom().nextInt(100_000_000)
                val pairingCode = String.format(
                    Locale.US,
                    "%0${PAIRING_CODE_DIGIT_COUNT}d",
                    pairingNumeric
                )
                val pairingUtf8 = pairingCode.toByteArray(StandardCharsets.UTF_8)
                val qr = LocalTransferQrPayload(
                    ip,
                    ss.localPort,
                    serverKey.publicKeyEncoded,
                    sessionVerifier
                )
                val json = qr.toJsonString()
                val bmp = withContext(Dispatchers.Default) { QrBitmapGenerator.encode(json) }
                _uiState.value = VaultReceiveUiState.Listening(qrBitmap = bmp, pairingCode = pairingCode)
                val temp = File(getApplication<Application>().cacheDir, "vault-in-${System.nanoTime()}.packed")
                val result = LocalVaultTransfer.receiveOnce(
                    ss,
                    serverKey,
                    temp,
                    TRANSFER_TIMEOUT_MS,
                    sessionVerifier,
                    pairingUtf8
                )
                if (result.isFailure) {
                    temp.delete()
                    val app = getApplication<Application>()
                    _uiState.value = VaultReceiveUiState.Failed(
                        result.exceptionOrNull()?.let { app.messageForVaultTransferFailure(it) }
                            ?: app.getString(R.string.settings_vault_error_receive)
                    )
                    return@launch
                }
                _uiState.value = VaultReceiveUiState.AwaitingPassword(temp)
            } catch (e: Exception) {
                val app = getApplication<Application>()
                _uiState.value = VaultReceiveUiState.Failed(app.messageForVaultTransferFailure(e))
            }
        }
    }

    fun importWithPassword(password: String) {
        val awaiting = _uiState.value as? VaultReceiveUiState.AwaitingPassword ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = VaultReceiveUiState.Importing
            val r = transferPort.importPackedVaultFile(awaiting.packedFile, password)
            awaiting.packedFile.delete()
            _uiState.value = if (r.isSuccess) {
                VaultReceiveUiState.Done
            } else {
                VaultReceiveUiState.Failed(
                    getApplication<Application>().getString(R.string.settings_vault_error_import)
                )
            }
        }
    }

    /** ViewModel 破棄時に待受ソケットを閉じる。 */
    override fun onCleared() {
        super.onCleared()
        runCatching { serverSocket?.close() }
    }

    private companion object {
        const val TRANSFER_TIMEOUT_MS = 120_000L
    }
}
