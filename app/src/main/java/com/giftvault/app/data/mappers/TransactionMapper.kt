package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.TransactionEntity
import com.giftvault.app.domain.model.Transaction
import com.giftvault.app.domain.model.TransactionType
import java.time.Instant

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = this.id,
        cardId = this.cardId,
        amount = this.amount,
        type = TransactionType.fromValue(this.type),
        note = this.note,
        createdAt = Instant.ofEpochMilli(this.createdAt)
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = this.id,
        cardId = this.cardId,
        amount = this.amount,
        type = this.type.value,
        note = this.note,
        createdAt = this.createdAt.toEpochMilli()
    )
}
