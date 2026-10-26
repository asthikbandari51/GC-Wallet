package com.giftvault.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "truztee_claims")
data class TruzteeClaimEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "order_id") val orderId: String,
    @ColumnInfo(name = "transaction_id") val transactionId: String,
    @ColumnInfo(name = "merchant_name") val merchantName: String,
    @ColumnInfo(name = "voucher_amount") val voucherAmount: Double,
    @ColumnInfo(name = "status") val status: String = "PENDING",
    @ColumnInfo(name = "bank_card_id") val bankCardId: Long? = null,
    @ColumnInfo(name = "bank_card_nickname") val bankCardNickname: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "resolved_at") val resolvedAt: Long? = null
)
