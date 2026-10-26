package com.giftvault.app.core.receipts

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraUtils {
    fun createTempCameraUri(context: Context): Uri {
        val cameraDir = File(context.cacheDir, "camera").apply {
            if (!exists()) mkdirs()
        }
        val tempFile = File(cameraDir, "tmp_capture_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempFile)
    }
}
