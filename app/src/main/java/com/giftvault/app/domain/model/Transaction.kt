package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class Transaction(
    val id: String,
    val cardId: String,
    val amount: Double,
    val type: TransactionType,
    val note: String?,
    val createdAt: Instant
)
