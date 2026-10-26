package com.giftvault.app.di

import android.content.Context
import androidx.room.Room
import com.giftvault.app.core.database.GiftVaultDatabase
import com.giftvault.app.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GiftVaultDatabase {
        return Room.databaseBuilder(
            context,
            GiftVaultDatabase::class.java,
            "gift_vault_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGiftCardDao(db: GiftVaultDatabase): GiftCardDao = db.giftCardDao()

    @Provides
    fun provideTransactionDao(db: GiftVaultDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideReminderDao(db: GiftVaultDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideReceiptDao(db: GiftVaultDatabase): ReceiptDao = db.receiptDao()

    @Provides
    fun provideBankCardDao(db: GiftVaultDatabase): BankCardDao = db.bankCardDao()

    @Provides
    fun provideTruzteeClaimDao(db: GiftVaultDatabase): TruzteeClaimDao = db.truzteeClaimDao()

    @Provides
    fun providePerkQuotaDao(db: GiftVaultDatabase): PerkQuotaDao = db.perkQuotaDao()
}
