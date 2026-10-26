package com.giftvault.app.core.receipts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessor @Inject constructor() {

    companion object {
        private const val MAX_DIMENSION = 1600
        private const val JPEG_QUALITY = 85
    }

    suspend fun processAndSaveImage(
        context: Context,
        cardId: String,
        imageUri: Uri
    ): File = withContext(Dispatchers.IO) {
        val cardReceiptsDir = File(context.filesDir, "receipts/$cardId").apply {
            if (!exists()) mkdirs()
        }

        val receiptId = UUID.randomUUID().toString()
        val outputFile = File(cardReceiptsDir, "$receiptId.jpg")

        val imageBytes = try {
            context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }

        if (imageBytes == null || imageBytes.isEmpty()) {
            throw IllegalArgumentException("Cannot read image content from Uri: $imageUri")
        }

        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(ByteArrayInputStream(imageBytes), null, options)

            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val decodedBitmap = BitmapFactory.decodeStream(ByteArrayInputStream(imageBytes), null, options)

            if (decodedBitmap != null) {
                val rotationAngle = getExifRotationFromBytes(imageBytes)
                val finalBitmap = if (rotationAngle != 0) {
                    val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                    val rotated = Bitmap.createBitmap(
                        decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true
                    )
                    if (rotated != decodedBitmap) decodedBitmap.recycle()
                    rotated
                } else {
                    decodedBitmap
                }

                FileOutputStream(outputFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                }
                finalBitmap.recycle()
            } else {
                // Direct byte copy fallback
                FileOutputStream(outputFile).use { out ->
                    out.write(imageBytes)
                }
            }
        } catch (e: Exception) {
            // Fallback: direct byte write if processing fails
            FileOutputStream(outputFile).use { out ->
                out.write(imageBytes)
            }
        }

        outputFile
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getExifRotationFromBytes(bytes: ByteArray): Int {
        return try {
            val exifInterface = ExifInterface(ByteArrayInputStream(bytes))
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}
