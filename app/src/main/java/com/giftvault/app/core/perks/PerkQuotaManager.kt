package com.giftvault.app.core.perks

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.giftvault.app.core.database.dao.PerkQuotaDao
import com.giftvault.app.core.database.entity.PerkQuotaEntity
import com.giftvault.app.core.database.entity.PerkType
import com.giftvault.app.core.database.entity.QuotaFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerkQuotaManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val perkQuotaDao: PerkQuotaDao
) {
    companion object {
        const val RUPAY_CONCIERGE_NUMBER = "18002667077"
    }

    /**
     * Calculates start and end timestamps for the current Indian financial/calendar quarter.
     * Q1: Jan 1 -> Mar 31, Q2: Apr 1 -> Jun 30, Q3: Jul 1 -> Sep 30, Q4: Oct 1 -> Dec 31
     */
    fun calculateCurrentQuarterBounds(epoch: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply { timeInMillis = epoch }
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed
        val quarterStartMonth = (currentMonth / 3) * 3

        calendar.set(Calendar.MONTH, quarterStartMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 3)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    /**
     * Seeds standard RuPay Select / Platinum entitlements for a bank card if not present.
     */
    suspend fun seedDefaultQuotasForCard(cardId: Long, isSelect: Boolean = true) = withContext(Dispatchers.IO) {
        val (qStart, qEnd) = calculateCurrentQuarterBounds()

        val calYear = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val yStart = calYear.timeInMillis
        calYear.add(Calendar.YEAR, 1)
        calYear.add(Calendar.MILLISECOND, -1)
        val yEnd = calYear.timeInMillis

        val defaultQuotas = if (isSelect) {
            listOf(
                PerkQuotaEntity(cardId = cardId, perkType = PerkType.LOUNGE_DOMESTIC.name, frequency = QuotaFrequency.QUARTERLY.name, totalEntitlement = 2, usedCount = 0, periodStartEpoch = qStart, periodEndEpoch = qEnd),
                PerkQuotaEntity(cardId = cardId, perkType = PerkType.SPA.name, frequency = QuotaFrequency.QUARTERLY.name, totalEntitlement = 1, usedCount = 0, periodStartEpoch = qStart, periodEndEpoch = qEnd),
                PerkQuotaEntity(cardId = cardId, perkType = PerkType.GOLF.name, frequency = QuotaFrequency.QUARTERLY.name, totalEntitlement = 1, usedCount = 0, periodStartEpoch = qStart, periodEndEpoch = qEnd),
                PerkQuotaEntity(cardId = cardId, perkType = PerkType.HEALTH_CHECK.name, frequency = QuotaFrequency.ANNUAL.name, totalEntitlement = 1, usedCount = 0, periodStartEpoch = yStart, periodEndEpoch = yEnd),
                PerkQuotaEntity(cardId = cardId, perkType = PerkType.GYM.name, frequency = QuotaFrequency.QUARTERLY.name, totalEntitlement = 12, usedCount = 0, periodStartEpoch = qStart, periodEndEpoch = qEnd)
            )
        } else {
            listOf(
                PerkQuotaEntity(cardId = cardId, perkType = PerkType.LOUNGE_DOMESTIC.name, frequency = QuotaFrequency.QUARTERLY.name, totalEntitlement = 1, usedCount = 0, periodStartEpoch = qStart, periodEndEpoch = qEnd)
            )
        }
        perkQuotaDao.insertAll(defaultQuotas)
    }

    /**
     * Direct Dialer Intent for RuPay Dedicated Concierge Desk.
     */
    fun createConciergeDialIntent(): Intent {
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$RUPAY_CONCIERGE_NUMBER")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Pre-populated SMS Booking Payload.
     */
    fun createSmsBookingIntent(
        perkType: String,
        first6Bin: String,
        last4Digits: String,
        preferredDate: String
    ): Intent {
        val payload = "BOOK $perkType $first6Bin $last4Digits $preferredDate"
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$RUPAY_CONCIERGE_NUMBER")
            putExtra("sms_body", payload)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
