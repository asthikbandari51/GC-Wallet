package com.giftvault.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.giftvault.app.core.database.dao.*
import com.giftvault.app.core.database.entity.*

@Database(
    entities = [
        GiftCardEntity::class,
        TransactionEntity::class,
        ReminderEntity::class,
        ReceiptEntity::class,
        TemplateEntity::class,
        BankCardEntity::class,
        TruzteeClaimEntity::class,
        PerkQuotaEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GiftVaultDatabase : RoomDatabase() {
    abstract fun giftCardDao(): GiftCardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun bankCardDao(): BankCardDao
    abstract fun truzteeClaimDao(): TruzteeClaimDao
    abstract fun perkQuotaDao(): PerkQuotaDao

    companion object {
        const val DATABASE_NAME = "gift_vault_database"
    }
}
