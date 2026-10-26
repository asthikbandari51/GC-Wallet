package com.giftvault.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = GiftCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "card_id") val cardId: String,
    @ColumnInfo(name = "trigger_date") val triggerDate: Long,
    @ColumnInfo(name = "window_days") val windowDays: Int,
    @ColumnInfo(name = "status") val status: String = "pending"
)
