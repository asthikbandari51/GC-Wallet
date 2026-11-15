package com.giftvault.app.core.rupay

import com.giftvault.app.data.repository.BankCardRepository
import com.giftvault.app.data.repository.CardRepository
import com.giftvault.app.domain.model.BankCard
import com.giftvault.app.domain.model.CardNetwork
import com.giftvault.app.domain.model.GiftCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class RupayBenefitCategory(
    val id: String,
    val title: String,
    val shortName: String,
    val merchantKeywords: List<String>,
    val fallbackCategories: List<String>,
    val quarterlyValue: Double,
    val description: String,
    val defaultPortalUrl: String = "https://rupayselect.truztee.com"
) {
    SPA_SALON(
        id = "spa",
        title = "Lakme Salon / Luxury Spa",
        shortName = "Lakme Salon",
        merchantKeywords = listOf("lakme", "salon", "spa", "enrich"),
        fallbackCategories = listOf("Spa & Salon", "SPA_SALON"),
        quarterlyValue = 1500.0,
        description = "Quarterly ₹1,500 beauty care voucher at Lakme Salon or Airport Spa."
    ),
    SHOPPING(
        id = "shopping",
        title = "Myntra / Reliance Shopping",
        shortName = "Myntra / Reliance",
        merchantKeywords = listOf("myntra", "reliance digital", "trends", "ajio"),
        fallbackCategories = listOf("Shopping", "Electronics", "SHOPPING", "ELECTRONICS"),
        quarterlyValue = 500.0,
        description = "Quarterly ₹500 off voucher on Myntra or Reliance Digital."
    ),
    FITNESS_SPORTS(
        id = "fitness",
        title = "Decathlon / Cult.fit",
        shortName = "Decathlon",
        merchantKeywords = listOf("decathlon", "cult", "cult.fit", "gym"),
        fallbackCategories = listOf("Sports", "Fitness", "SPORTS"),
        quarterlyValue = 500.0,
        description = "Quarterly ₹500 Decathlon gear voucher or 1-month Cult gym pass."
    ),
    GROCERIES(
        id = "groceries",
        title = "BigBasket Groceries",
        shortName = "BigBasket",
        merchantKeywords = listOf("bigbasket", "bb", "grofers", "blinkit", "zepto"),
        fallbackCategories = listOf("Groceries", "GROCERIES"),
        quarterlyValue = 250.0,
        description = "Quarterly flat ₹250 off voucher on BigBasket groceries."
    ),
    ENTERTAINMENT_OTT(
        id = "entertainment",
        title = "BookMyShow / OTT",
        shortName = "BookMyShow / OTT",
        merchantKeywords = listOf("bookmyshow", "bms", "hotstar", "jiohotstar", "gaana", "sony", "prime"),
        fallbackCategories = listOf("Entertainment", "OTT & Streaming", "Music", "ENTERTAINMENT", "OTT_STREAMING"),
        quarterlyValue = 200.0,
        description = "Quarterly BookMyShow movie vouchers (₹200) or streaming subscriptions."
    ),
    HEALTH_WELLNESS(
        id = "health",
        title = "Apollo Pharmacy / Health Check",
        shortName = "Apollo Pharmacy",
        merchantKeywords = listOf("apollo", "pharmacy", "health", "pharmeasy", "1mg"),
        fallbackCategories = listOf("Pharmacy", "Health & Wellness", "PHARMACY"),
        quarterlyValue = 250.0,
        description = "Quarterly Apollo Pharmacy voucher / preventive health checkup."
    );

    fun matchesVoucher(voucher: GiftCard): Boolean {
        val mLower = voucher.merchantName.lowercase()
        if (merchantKeywords.any { mLower.contains(it) }) return true
        val cLower = voucher.category.displayName.lowercase()
        return fallbackCategories.any { it.equals(voucher.category.displayName, ignoreCase = true) || cLower.contains(it.lowercase()) }
    }
}

data class BenefitClaimStatus(
    val category: RupayBenefitCategory,
    val isClaimed: Boolean,
    val matchingVoucher: GiftCard? = null,
    val claimQuarter: String,
    val claimDateText: String? = null
)

data class PnbCardBenefitSummary(
    val card: BankCard,
    val quarterLabel: String,
    val benefits: List<BenefitClaimStatus>,
    val claimedCount: Int,
    val totalCount: Int,
    val totalClaimedValue: Double,
    val totalPotentialValue: Double
)

@Singleton
class RupayBenefitTracker @Inject constructor(
    private val cardRepository: CardRepository,
    private val bankCardRepository: BankCardRepository
) {
    companion object {
        fun getCurrentQuarterLabel(): String {
            val cal = Calendar.getInstance()
            val month = cal.get(Calendar.MONTH) // 0-indexed
            val q = (month / 3) + 1
            val year = cal.get(Calendar.YEAR)
            return "Q$q $year"
        }
    }

    val benefitSummaries: Flow<List<PnbCardBenefitSummary>> = combine(
        bankCardRepository.getAllBankCards(),
        cardRepository.getAllCards()
    ) { bankCards, giftCards ->
        // Target RuPay cards, specifically PNB Imperial or any RuPay Select card
        val eligibleCards = bankCards.filter { bCard ->
            bCard.network == CardNetwork.RUPAY ||
            bCard.bankName.contains("PNB", ignoreCase = true) ||
            bCard.cardNickname.contains("Imperial", ignoreCase = true)
        }

        val quarter = getCurrentQuarterLabel()

        eligibleCards.map { card ->
            // Active vouchers linked to this specific bank card
            val linkedVouchers = giftCards.filter { gCard ->
                gCard.linkedCardId == card.id &&
                gCard.status != com.giftvault.app.domain.model.CardStatus.ARCHIVED
            }

            val categoryStatuses = RupayBenefitCategory.entries.map { cat ->
                val matched = linkedVouchers.find { cat.matchesVoucher(it) }
                BenefitClaimStatus(
                    category = cat,
                    isClaimed = matched != null,
                    matchingVoucher = matched,
                    claimQuarter = quarter,
                    claimDateText = matched?.createdAt?.let {
                        DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(java.time.ZoneId.systemDefault()).format(it)
                    }
                )
            }

            val claimedCount = categoryStatuses.count { it.isClaimed }
            val totalCount = categoryStatuses.size
            val claimedValue = categoryStatuses.filter { it.isClaimed }.sumOf { it.matchingVoucher?.initialValue ?: it.category.quarterlyValue }
            val potentialValue = categoryStatuses.sumOf { it.category.quarterlyValue }

            PnbCardBenefitSummary(
                card = card,
                quarterLabel = quarter,
                benefits = categoryStatuses,
                claimedCount = claimedCount,
                totalCount = totalCount,
                totalClaimedValue = claimedValue,
                totalPotentialValue = potentialValue
            )
        }
    }

    suspend fun linkVoucherToCard(voucherId: String, bankCardId: Long) {
        val voucher = cardRepository.getCardById(voucherId)
        // Link voucher to the bank card
        voucher.collect { card ->
            if (card != null) {
                cardRepository.updateCard(card.copy(linkedCardId = bankCardId))
            }
        }
    }
}
