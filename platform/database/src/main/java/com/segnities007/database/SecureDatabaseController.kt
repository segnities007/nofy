package com.segnities007.database

/**
 * SQLCipher 等の暗号化データベースのロック／アンロック・パスフレーズ変更・ファイル削除を制御する。
 */
interface SecureDatabaseController {
    /** [passphrase] で暗号化 DB を開く（SQLCipher のキー供給）。 */
    fun unlock(passphrase: ByteArray)

    /** 現在のパスフレーズを検証したうえで DB の暗号キーを変更する。 */
    fun changePassphrase(currentPassphrase: String, newPassphrase: String)

    /** 接続を閉じ、メモリ上の鍵を破棄する。 */
    fun lock()

    /** アプリの DB ファイルを削除する（リセット時など）。 */
    fun deleteDatabaseFiles()
}
