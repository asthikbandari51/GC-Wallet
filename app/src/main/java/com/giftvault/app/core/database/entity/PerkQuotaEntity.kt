package com.giftvault.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class PerkType(val displayName: String) {
    LOUNGE_DOMESTIC("Domestic Airport Lounge"),
    LOUNGE_INTL("International Airport Lounge"),
    SPA("Airport / Luxury Spa"),
    GOLF("Golf Games & Lessons"),
    GYM("Gym Access (Cult/Fit)"),
    HEALTH_CHECK("Preventive Health Checkup"),
    OTT_ENTERTAINMENT("OTT Subscription")
}

@Serializable
enum class QuotaFrequency(val displayName: String) {
    QUARTERLY("Quarterly"),
    ANNUAL("Calendar Year")
}

@Serializable
@Entity(
    tableName = "perk_quotas",
    foreignKeys = [
        ForeignKey(
            entity = BankCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("card_id")]
)
data class PerkQuotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "card_id") val cardId: Long,
    @ColumnInfo(name = "perk_type") val perkType: String,
    @ColumnInfo(name = "frequency") val frequency: String,
    @ColumnInfo(name = "total_entitlement") val totalEntitlement: Int,
    @ColumnInfo(name = "used_count") val usedCount: Int = 0,
    @ColumnInfo(name = "period_start_epoch") val periodStartEpoch: Long,
    @ColumnInfo(name = "period_end_epoch") val periodEndEpoch: Long,
    @ColumnInfo(name = "booking_reference") val bookingReference: String? = null
)
