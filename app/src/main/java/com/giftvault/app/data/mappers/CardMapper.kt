package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.GiftCardEntity
import com.giftvault.app.domain.model.CardCategory
import com.giftvault.app.domain.model.CardStatus
import com.giftvault.app.domain.model.GiftCard
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

fun GiftCardEntity.toDomain(decryptedNumber: String, decryptedPin: String?): GiftCard {
    return GiftCard(
        id = this.id,
        merchantName = this.merchantName,
        templateKey = this.templateKey,
        cardNumber = decryptedNumber,
        pin = decryptedPin,
        balance = this.balance,
        initialValue = this.initialValue,
        currency = this.currency,
        expiryDate = this.expiryDate?.let { 
            Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate() 
        },
        category = CardCategory.fromDisplayName(this.category),
        notes = this.notes,
        status = CardStatus.fromValue(this.status),
        isFavorite = this.isFavorite,
        linkedCardId = this.linkedCardId,
        createdAt = Instant.ofEpochMilli(this.createdAt),
        updatedAt = Instant.ofEpochMilli(this.updatedAt)
    )
}

fun GiftCard.toEntity(encryptedNumber: String, encryptedPin: String?): GiftCardEntity {
    return GiftCardEntity(
        id = this.id,
        merchantName = this.merchantName,
        templateKey = this.templateKey,
        encryptedCardNumber = encryptedNumber,
        encryptedPin = encryptedPin,
        balance = this.balance,
        initialValue = this.initialValue,
        currency = this.currency,
        expiryDate = this.expiryDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        category = this.category.displayName,
        notes = this.notes,
        status = this.status.value,
        isFavorite = this.isFavorite,
        linkedCardId = this.linkedCardId,
        createdAt = this.createdAt.toEpochMilli(),
        updatedAt = this.updatedAt.toEpochMilli()
    )
}
