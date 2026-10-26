package com.giftvault.app.core.security

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SteganographyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val MAGIC_BYTES = byteArrayOf(0x47, 0x56, 0x53, 0x54) // 'GVST'
        const val HEADER_SIZE = 32 // 4 bytes magic + 4 bytes length + 24 bytes salt
    }

    /**
     * Calculates the maximum hidden payload size in bytes that can fit into a bitmap of [width] x [height].
     */
    fun maxPayloadBytes(width: Int, height: Int): Int {
        return ((width * height * 3) / 8) - HEADER_SIZE
    }

    /**
     * Creates an encrypted payload with 32-byte header:
     * - 4 bytes: GVST
     * - 4 bytes: Payload length (Big Endian)
     * - 24 bytes: Salt
     * - Followed by AES-256-GCM: 12-byte IV + ciphertext + 16-byte tag.
     */
    fun encryptPayload(plainData: ByteArray, password: String): ByteArray {
        val salt = ByteArray(24)
        SecureRandom().nextBytes(salt)

        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plainData)

        // Combined stream: IV (12) + Ciphertext + Tag
        val encryptedStream = ByteBuffer.allocate(iv.size + ciphertext.size)
            .put(iv)
            .put(ciphertext)
            .array()

        // Construct 32-byte header
        val header = ByteBuffer.allocate(HEADER_SIZE)
            .put(MAGIC_BYTES)
            .putInt(encryptedStream.size)
            .put(salt)
            .array()

        return ByteBuffer.allocate(header.size + encryptedStream.size)
            .put(header)
            .put(encryptedStream)
            .array()
    }

    /**
     * Embeds the raw binary [fullPayload] (header + ciphertext) into the LSB of R, G, B channels of [carrier].
     */
    fun embedPayloadIntoBitmap(carrier: Bitmap, fullPayload: ByteArray): Bitmap {
        val width = carrier.width
        val height = carrier.height
        val totalPixels = width * height
        val totalAvailableBits = totalPixels * 3
        val requiredBits = fullPayload.size * 8

        require(requiredBits <= totalAvailableBits) {
            "Carrier image is too small to hold payload ($requiredBits bits needed, $totalAvailableBits available)"
        }

        val pixels = IntArray(totalPixels)
        carrier.getPixels(pixels, 0, width, 0, 0, width, height)

        var bitIndex = 0

        for (i in 0 until totalPixels) {
            if (bitIndex >= requiredBits) break

            var pixel = pixels[i]
            val a = (pixel ushr 24) and 0xFF
            var r = (pixel ushr 16) and 0xFF
            var g = (pixel ushr 8) and 0xFF
            var b = pixel and 0xFF

            // Red channel LSB
            if (bitIndex < requiredBits) {
                val bytePos = bitIndex / 8
                val bitPos = 7 - (bitIndex % 8)
                val bit = (fullPayload[bytePos].toInt() ushr bitPos) and 1
                r = (r and 0xFE) or bit
                bitIndex++
            }

            // Green channel LSB
            if (bitIndex < requiredBits) {
                val bytePos = bitIndex / 8
                val bitPos = 7 - (bitIndex % 8)
                val bit = (fullPayload[bytePos].toInt() ushr bitPos) and 1
                g = (g and 0xFE) or bit
                bitIndex++
            }

            // Blue channel LSB
            if (bitIndex < requiredBits) {
                val bytePos = bitIndex / 8
                val bitPos = 7 - (bitIndex % 8)
                val bit = (fullPayload[bytePos].toInt() ushr bitPos) and 1
                b = (b and 0xFE) or bit
                bitIndex++
            }

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val stegoBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        stegoBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return stegoBitmap
    }

    /**
     * Extracts and decrypts payload from a steganographic carrier [stegoBitmap] with [password].
     */
    fun extractAndDecryptPayload(stegoBitmap: Bitmap, password: String): ByteArray {
        val width = stegoBitmap.width
        val height = stegoBitmap.height
        val totalPixels = width * height

        val pixels = IntArray(totalPixels)
        stegoBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 1. Read 32-byte header (256 bits)
        val headerBits = HEADER_SIZE * 8
        val headerBytes = ByteArray(HEADER_SIZE)
        var bitIndex = 0

        for (i in 0 until totalPixels) {
            if (bitIndex >= headerBits) break
            val pixel = pixels[i]
            val channels = intArrayOf((pixel ushr 16) and 0xFF, (pixel ushr 8) and 0xFF, pixel and 0xFF)

            for (ch in channels) {
                if (bitIndex < headerBits) {
                    val bit = ch and 1
                    val bytePos = bitIndex / 8
                    val bitPos = 7 - (bitIndex % 8)
                    headerBytes[bytePos] = (headerBytes[bytePos].toInt() or (bit shl bitPos)).toByte()
                    bitIndex++
                }
            }
        }

        val headerBuffer = ByteBuffer.wrap(headerBytes)
        val magic = ByteArray(4)
        headerBuffer.get(magic)

        if (!magic.contentEquals(MAGIC_BYTES)) {
            throw IllegalArgumentException("Not a valid GiftVault steganographic image (Magic marker missing)")
        }

        val payloadLength = headerBuffer.int
        if (payloadLength <= 0 || payloadLength > (totalPixels * 3) / 8) {
            throw IllegalArgumentException("Corrupted or invalid hidden vault payload length: $payloadLength")
        }

        val salt = ByteArray(24)
        headerBuffer.get(salt)

        // 2. Read ciphertext payload
        val totalBitsToRead = (HEADER_SIZE + payloadLength) * 8
        val encryptedBytes = ByteArray(payloadLength)

        var payloadBitIndex = 0
        bitIndex = 0

        for (i in 0 until totalPixels) {
            if (bitIndex >= totalBitsToRead) break
            val pixel = pixels[i]
            val channels = intArrayOf((pixel ushr 16) and 0xFF, (pixel ushr 8) and 0xFF, pixel and 0xFF)

            for (ch in channels) {
                if (bitIndex >= headerBits && bitIndex < totalBitsToRead) {
                    val bit = ch and 1
                    val bytePos = payloadBitIndex / 8
                    val bitPos = 7 - (payloadBitIndex % 8)
                    encryptedBytes[bytePos] = (encryptedBytes[bytePos].toInt() or (bit shl bitPos)).toByte()
                    payloadBitIndex++
                }
                bitIndex++
            }
        }

        // 3. Decrypt payload (First 12 bytes are IV, rest is ciphertext + tag)
        if (encryptedBytes.size < 28) {
            throw IllegalArgumentException("Ciphertext payload is truncated")
        }

        val iv = encryptedBytes.copyOfRange(0, 12)
        val ciphertext = encryptedBytes.copyOfRange(12, encryptedBytes.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    /**
     * Generates a scenic gradient carrier bitmap if the user does not have an existing photo to use.
     */
    fun createScenicCarrierBitmap(width: Int = 1600, height: Int = 1200): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }

        // Sky & Nature Gradient
        val shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.parseColor("#1A2980"), Color.parseColor("#26D0CE"), Color.parseColor("#6A11CB")),
            floatArrayOf(0f, 0.6f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Procedural decorative landscape circles for high spatial entropy
        paint.shader = null
        val rng = SecureRandom(byteArrayOf(42))
        for (i in 0 until 40) {
            paint.color = Color.argb(40 + rng.nextInt(60), rng.nextInt(256), rng.nextInt(256), rng.nextInt(256))
            val cx = rng.nextFloat() * width
            val cy = rng.nextFloat() * height
            val radius = 30f + rng.nextFloat() * 180f
            canvas.drawCircle(cx, cy, radius, paint)
        }

        return bitmap
    }

    /**
     * Saves a lossless PNG to device Pictures / DCIM gallery.
     */
    suspend fun saveStegoImageToGallery(bitmap: Bitmap, filename: String = "IMG_VAULT_${System.currentTimeMillis()}.png"): Uri? = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GiftVault")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                    }
                    return@withContext uri
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GiftVault")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
                return@withContext Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}
