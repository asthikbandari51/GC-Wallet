package com.giftvault.app.core.templates

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlin.math.abs

object ColorEngine {

    fun calculateLuminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0

        val rL = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gL = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bL = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        return 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
    }

    fun contrastRatio(l1: Double, l2: Double): Double {
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    fun sampleZoneLuminance(bitmap: Bitmap, zone: TextZone): Double {
        var totalLuminance = 0.0
        var count = 0

        val startX = zone.x.coerceIn(0, bitmap.width - 1)
        val startY = zone.y.coerceIn(0, bitmap.height - 1)
        val endX = (zone.x + zone.w).coerceIn(0, bitmap.width)
        val endY = (zone.y + zone.h).coerceIn(0, bitmap.height)

        if (startX >= endX || startY >= endY) return 0.0

        for (x in startX until endX step 5) {
            for (y in startY until endY step 5) {
                val pixel = bitmap.getPixel(x, y)
                totalLuminance += calculateLuminance(pixel)
                count++
            }
        }

        return if (count > 0) totalLuminance / count else 0.0
    }

    fun getTextColorForZone(bitmap: Bitmap, zone: TextZone): ComposeColor {
        val luminance = sampleZoneLuminance(bitmap, zone)
        return getTextColor(luminance)
    }

    fun getTextColor(backgroundLuminance: Double): ComposeColor {
        return if (backgroundLuminance > 0.179) ComposeColor(0xFF111111) else ComposeColor(0xFFF8F9FA)
    }

    /**
     * Generates a deterministic, vibrant procedural gradient brush based on an ID / merchant string.
     */
    fun getProceduralGradient(seed: String): Brush {
        val hash = abs(seed.hashCode())
        val palettes = listOf(
            listOf(ComposeColor(0xFF4A00E0), ComposeColor(0xFF8E2DE2), ComposeColor(0xFF00D2FF)),
            listOf(ComposeColor(0xFF0F2027), ComposeColor(0xFF203A43), ComposeColor(0xFF2C5364)),
            listOf(ComposeColor(0xFF11998E), ComposeColor(0xFF38EF7D), ComposeColor(0xFF0575E6)),
            listOf(ComposeColor(0xFF8A2387), ComposeColor(0xFFE94057), ComposeColor(0xFFF27121)),
            listOf(ComposeColor(0xFF1A2980), ComposeColor(0xFF26D0CE), ComposeColor(0xFF1F4037)),
            listOf(ComposeColor(0xFF3A1C71), ComposeColor(0xFFD76D77), ComposeColor(0xFFFFAF7B)),
            listOf(ComposeColor(0xFF20002C), ComposeColor(0xFFCBB4D4), ComposeColor(0xFF4B1248)),
            listOf(ComposeColor(0xFF1E3C72), ComposeColor(0xFF2A5298), ComposeColor(0xFF7928CA))
        )
        val chosen = palettes[hash % palettes.size]
        return Brush.linearGradient(chosen)
    }
}
