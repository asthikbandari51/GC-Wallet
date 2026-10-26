package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class Receipt(
    val id: String,
    val cardId: String,
    val filePath: String,
    val createdAt: Instant
)
