package com.segnities007.setting.presentation.vault

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/** 接続用文字列を ZXing で QR ビットマップにエンコードする。 */
internal object QrBitmapGenerator {
    fun encode(content: String, size: Int = 480): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bmp
    }
}
