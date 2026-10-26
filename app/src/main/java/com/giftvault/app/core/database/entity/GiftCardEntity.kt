package com.giftvault.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "gift_cards")
data class GiftCardEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "merchant_name") val merchantName: String,
    @ColumnInfo(name = "template_key") val templateKey: String,
    @ColumnInfo(name = "encrypted_card_number") val encryptedCardNumber: String,
    @ColumnInfo(name = "encrypted_pin") val encryptedPin: String?,
    @ColumnInfo(name = "balance") val balance: Double,
    @ColumnInfo(name = "initial_value") val initialValue: Double,
    @ColumnInfo(name = "currency") val currency: String = "INR",
    @ColumnInfo(name = "expiry_date") val expiryDate: Long?,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "status") val status: String = "active",
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "linked_card_id") val linkedCardId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
