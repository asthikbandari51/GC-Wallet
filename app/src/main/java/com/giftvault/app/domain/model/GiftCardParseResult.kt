package com.giftvault.app.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class GiftCardParseResult(
    val merchantName: String? = null,
    val templateKey: String? = null,
    val cardCode: String? = null,
    val pin: String? = null,
    val amount: Double? = null,
    val issueDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val service: String? = null,
    val details: String? = null,
    val sourceType: ParseSourceType = ParseSourceType.MANUAL_TEXT,
    val confidenceScore: Float = 0.0f,
    val fieldConfidences: Map<String, Float> = emptyMap(),
    val rawText: String = "",
    val imageUri: Uri? = null,
    val isDuplicate: Boolean = false,
    val duplicateCardId: String? = null
)
