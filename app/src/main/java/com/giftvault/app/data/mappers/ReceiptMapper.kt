package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.ReceiptEntity
import com.giftvault.app.domain.model.Receipt
import java.time.Instant

fun ReceiptEntity.toDomain(): Receipt = Receipt(
    id = id,
    cardId = cardId,
    filePath = filePath,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    cardId = cardId,
    filePath = filePath,
    createdAt = createdAt.toEpochMilli()
)
