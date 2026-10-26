package com.giftvault.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "bank_cards")
data class BankCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "card_nickname") val cardNickname: String,
    @ColumnInfo(name = "bank_name") val bankName: String,
    @ColumnInfo(name = "card_type", defaultValue = "CREDIT") val cardType: String = "CREDIT",
    @ColumnInfo(name = "network") val network: String,
    @ColumnInfo(name = "tier") val tier: String,
    @ColumnInfo(name = "encrypted_card_number") val encryptedCardNumber: String,
    @ColumnInfo(name = "encrypted_cvv") val encryptedCvv: String,
    @ColumnInfo(name = "encrypted_expiry_month") val encryptedExpiryMonth: String,
    @ColumnInfo(name = "encrypted_expiry_year") val encryptedExpiryYear: String,
    @ColumnInfo(name = "last_four_digits") val lastFourDigits: String,
    @ColumnInfo(name = "cardholder_name") val cardholderName: String,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean = false,
    @ColumnInfo(name = "quarterly_spend_target") val quarterlySpendTarget: Double = 0.0,
    @ColumnInfo(name = "current_quarter_spend") val currentQuarterSpend: Double = 0.0
)
