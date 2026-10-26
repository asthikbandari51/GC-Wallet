package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant
import java.time.LocalDate

@Immutable
data class GiftCard(
    val id: String,
    val merchantName: String,
    val templateKey: String,
    val cardNumber: String,
    val pin: String?,
    val balance: Double,
    val initialValue: Double,
    val currency: String,
    val expiryDate: LocalDate?,
    val category: CardCategory,
    val notes: String?,
    val status: CardStatus,
    val isFavorite: Boolean,
    val linkedCardId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isExpired: Boolean
        get() = expiryDate != null && expiryDate.isBefore(LocalDate.now())

    val isFullyRedeemed: Boolean
        get() = balance <= 0.0

    val usagePercentage: Float
        get() = if (initialValue > 0) {
            ((initialValue - balance) / initialValue * 100f).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }

    val maskedCardNumber: String
        get() = if (cardNumber.length > 4) {
            "•".repeat(cardNumber.length - 4) + cardNumber.takeLast(4)
        } else {
            cardNumber
        }
}
