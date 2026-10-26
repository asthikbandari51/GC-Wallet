package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
enum class ClaimStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    RESOLVED("Resolved"),
    DISPUTED("Disputed");

    companion object {
        fun fromString(value: String?): ClaimStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

@Immutable
data class TruzteeClaim(
    val id: Long = 0L,
    val orderId: String,
    val transactionId: String,
    val merchantName: String,
    val voucherAmount: Double,
    val status: ClaimStatus = ClaimStatus.PENDING,
    val bankCardId: Long? = null,
    val bankCardNickname: String? = null,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val resolvedAt: Instant? = null
)
