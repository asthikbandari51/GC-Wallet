package com.giftvault.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gift_cards ADD COLUMN amount_paid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE gift_cards ADD COLUMN custom_finish TEXT NOT NULL DEFAULT 'DEFAULT'")
                db.execSQL("ALTER TABLE gift_cards ADD COLUMN badge_tag TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE gift_cards ADD COLUMN is_decoy INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE bank_cards ADD COLUMN custom_finish TEXT NOT NULL DEFAULT 'DEFAULT'")
                db.execSQL("ALTER TABLE bank_cards ADD COLUMN badge_tag TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE bank_cards ADD COLUMN is_decoy INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
