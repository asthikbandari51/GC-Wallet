package com.giftvault.app.core.sharing

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.giftvault.app.core.templates.TemplateRegistry
import com.giftvault.app.core.theme.ThemeManager
import com.giftvault.app.domain.model.GiftCard
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

enum class ShareMode {
    TRUSTED_UNMASKED,
    VISUAL_CARD
}

@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager? = null
) {

    private fun resolveUserName(): String {
        themeManager?.userName?.value?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
        val prefs = context.getSharedPreferences("giftvault_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("user_name", null)?.trim()
        if (!savedName.isNullOrBlank() && !savedName.equals("User", ignoreCase = true)) {
            return savedName
        }
        return "User"
    }

    fun shareCard(card: GiftCard, mode: ShareMode = ShareMode.TRUSTED_UNMASKED) {
        if (mode == ShareMode.VISUAL_CARD) {
            shareAsCardImage(card)
            return
        }

        val userName = resolveUserName()
        val pinText = if (!card.pin.isNullOrBlank()) "\nPIN: ${card.pin}" else ""
        val expiryText = if (card.expiryDate != null) "\nExpiry Date: ${card.expiryDate}" else ""
        val notesText = if (!card.notes.isNullOrBlank()) "\nNotes: ${card.notes}" else ""

        val shareText = "\uD83C\uDF81 GIFT CARD DETAILS \uD83C\uDF81\n" +
            "Merchant: ${card.merchantName}\n" +
            "Card Code: ${card.cardNumber}$pinText\n" +
            "Balance: ${card.currency} ${String.format("%.2f", card.balance)}$expiryText$notesText\n\n" +
            "Redeem directly in-store or online!\n" +
            "GV shared by \"$userName\""

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Gift card voucher for \"${card.merchantName}\"")
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share Gift Card").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun shareAsCardImage(card: GiftCard) {
        try {
            val width = 1080
            val height = 680
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Load brand template PNG as background
            val templateManifest = TemplateRegistry.getTemplate(card.templateKey)
            val isLight = templateManifest?.isLightTemplate ?: false
            val resId = context.resources.getIdentifier(
                card.templateKey, "drawable", context.packageName
            )

            if (resId != 0) {
                val bgBitmap = BitmapFactory.decodeResource(context.resources, resId)
                if (bgBitmap != null) {
                    val bgPaint = Paint().apply { isAntiAlias = true; isFilterBitmap = true }
                    canvas.drawBitmap(
                        bgBitmap,
                        Rect(0, 0, bgBitmap.width, bgBitmap.height),
                        Rect(0, 0, width, height),
                        bgPaint
                    )
                    bgBitmap.recycle()
                }
            } else {
                // Fallback gradient for unknown templates
                val gradPaint = Paint().apply {
                    isAntiAlias = true
                    shader = LinearGradient(
                        0f, 0f, width.toFloat(), height.toFloat(),
                        Color.parseColor("#4A00E0"), Color.parseColor("#8E2DE2"),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRoundRect(
                    RectF(0f, 0f, width.toFloat(), height.toFloat()),
                    40f, 40f, gradPaint
                )
            }

            // High contrast text colors without any grey bottom overlay!
            val textColor = if (isLight) Color.parseColor("#111111") else Color.WHITE
            val subTextColor = if (isLight) Color.parseColor("#333333") else Color.parseColor("#E2E8F0")

            val paint = Paint().apply {
                isAntiAlias = true
                if (!isLight) {
                    setShadowLayer(8f, 0f, 2f, Color.parseColor("#CC000000"))
                } else {
                    setShadowLayer(4f, 0f, 1f, Color.parseColor("#44FFFFFF"))
                }
            }

            // ── Card Code (center, full unmasked) ──
            paint.color = textColor
            paint.textSize = 50f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            val formattedCode = card.cardNumber.chunked(4).joinToString(" ")
            val codeWidth = paint.measureText(formattedCode)

            // Gentle rounded badge pill behind card code for maximum readability
            val pillPaint = Paint().apply {
                isAntiAlias = true
                color = if (isLight) Color.parseColor("#D9FFFFFF") else Color.parseColor("#8C000000")
            }
            val pillPaddingH = 32f
            val pillPaddingV = 16f
            val codeY = height * 0.52f
            val pillRect = RectF(
                (width - codeWidth) / 2f - pillPaddingH,
                codeY - 45f - pillPaddingV,
                (width + codeWidth) / 2f + pillPaddingH,
                codeY + 15f + pillPaddingV
            )
            canvas.drawRoundRect(pillRect, 20f, 20f, pillPaint)
            canvas.drawText(formattedCode, (width - codeWidth) / 2f, codeY, paint)

            // ── PIN ──
            if (!card.pin.isNullOrBlank()) {
                paint.textSize = 30f
                paint.color = subTextColor
                val pinStr = "PIN: ${card.pin}"
                val pinWidth = paint.measureText(pinStr)
                canvas.drawText(pinStr, (width - pinWidth) / 2f, height * 0.64f, paint)
            }

            // ── Balance (bottom left) ──
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 24f
            paint.color = subTextColor
            canvas.drawText("BALANCE", 60f, height * 0.82f, paint)
            paint.textSize = 46f
            paint.color = textColor
            canvas.drawText(
                "${card.currency} ${String.format("%.2f", card.balance)}",
                60f, height * 0.91f, paint
            )

            // ── Expiry (bottom right) ──
            if (card.expiryDate != null) {
                paint.textSize = 24f
                paint.color = subTextColor
                val vtLabel = "VALID THRU"
                canvas.drawText(vtLabel, width - 60f - paint.measureText(vtLabel), height * 0.82f, paint)
                paint.textSize = 36f
                paint.color = textColor
                val expStr = card.expiryDate.toString()
                canvas.drawText(expStr, width - 60f - paint.measureText(expStr), height * 0.91f, paint)
            }

            // ── Merchant Name (only for generic/other template) ──
            if (card.templateKey == "other" || card.templateKey.isBlank() || card.templateKey.endsWith("_other")) {
                paint.textSize = 46f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = textColor
                canvas.drawText(card.merchantName.uppercase(), 60f, 80f, paint)
            }

            // ── GIFT CARD badge (top right) ──
            paint.textSize = 24f
            paint.color = subTextColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val badge = "GIFT CARD"
            canvas.drawText(badge, width - 60f - paint.measureText(badge), 50f, paint)

            // ── Watermark with User Attribution ──
            val userName = resolveUserName()
            paint.textSize = 22f
            paint.color = subTextColor
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val watermark = "GV shared by \"$userName\""
            canvas.drawText(watermark, (width - paint.measureText(watermark)) / 2f, height - 20f, paint)

            // Save PNG and share
            val sharesDir = File(context.cacheDir, "shares").apply { if (!exists()) mkdirs() }
            val shareFile = File(sharesDir, "giftcard_${card.id}.png")
            FileOutputStream(shareFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            val imageUri: Uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", shareFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "Share Gift Card Image").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            // Fallback to plain text share
            shareCard(card, ShareMode.TRUSTED_UNMASKED)
        }
    }
}
