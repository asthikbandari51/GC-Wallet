package com.giftvault.app.core.parser

import com.giftvault.app.domain.model.ParseSourceType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GiftCardTextParserTest {

    private lateinit var parser: GiftCardTextParser

    @Before
    fun setUp() {
        parser = GiftCardTextParser()
    }

    @Test
    fun parseText_example1Format_extractsAllFieldsCorrectly() {
        val rawText = """
            Coupon Code:
            GYFRR34174757135
            Date of Issue:
            01-Jul-2026
            Service:
            Rupay - Online Shopping
            Service Provider:
            Reliance Digital
            Service/Product Details:
            Reliance Digital Offline- Quarterly
        """.trimIndent()

        val result = parser.parseText(rawText, ParseSourceType.PASTED_TEXT)

        assertEquals("GYFRR34174757135", result.cardCode)
        assertEquals(LocalDate.of(2026, 7, 1), result.issueDate)
        assertEquals("Rupay - Online Shopping", result.service)
        assertEquals("Reliance Digital", result.merchantName)
        assertEquals("reliancedigital", result.templateKey)
        assertEquals("Reliance Digital Offline- Quarterly", result.details)
        assertTrue("Confidence score should be high", result.confidenceScore >= 0.7f)
    }

    @Test
    fun parseText_example2Format_extractsCodePinAmountAndMerchant() {
        val rawText = """
            Coupon Code:
            9802004325817067/pin:898443
            Date of Issue:
            08-Jun-2026
            Service:
            Rupay - Online Pharmacy
            Service Provider:
            Apollo Pharmacy
            Service/Product Details:
            Apollo Pharmacy INR 250 Offline - Quarterly
        """.trimIndent()

        val result = parser.parseText(rawText, ParseSourceType.PASTED_TEXT)

        assertEquals("9802004325817067", result.cardCode)
        assertEquals("898443", result.pin)
        assertEquals(LocalDate.of(2026, 6, 8), result.issueDate)
        assertEquals("Rupay - Online Pharmacy", result.service)
        assertEquals("Apollo Pharmacy", result.merchantName)
        assertEquals("apollopharmacy", result.templateKey)
        assertEquals(250.0, result.amount!!, 0.01)
        assertTrue("Confidence score should be high", result.confidenceScore >= 0.8f)
    }

    @Test
    fun parseText_inlineCodeAndPin_extractsCorrectly() {
        val rawText = "Voucher Code: GYFR12345678 PIN: 9988 Amount: INR 500"
        val result = parser.parseText(rawText)

        assertEquals("GYFR12345678", result.cardCode)
        assertEquals("9988", result.pin)
        assertEquals(500.0, result.amount!!, 0.01)
    }

    @Test
    fun parseDate_validFormats_parsesLocalDate() {
        assertEquals(LocalDate.of(2026, 7, 1), ParsePatterns.parseDate("01-Jul-2026"))
        assertEquals(LocalDate.of(2026, 6, 8), ParsePatterns.parseDate("08-Jun-2026"))
        assertEquals(LocalDate.of(2026, 7, 1), ParsePatterns.parseDate("2026-07-01"))
        assertEquals(LocalDate.of(2026, 6, 8), ParsePatterns.parseDate("08/06/2026"))
        assertEquals(LocalDate.of(2026, 12, 1), ParsePatterns.parseDate("12/26"))
    }

    @Test
    fun parseAmount_variousCurrencyFormats_parsesDouble() {
        assertEquals(250.0, ParsePatterns.parseAmount("INR 250")!!, 0.01)
        assertEquals(500.0, ParsePatterns.parseAmount("₹500.00")!!, 0.01)
        assertEquals(1000.0, ParsePatterns.parseAmount("Rs. 1,000")!!, 0.01)
        assertEquals(250.0, ParsePatterns.parseAmount("250 rupees")!!, 0.01)
    }

    @Test
    fun parseText_merchantRegistryMatching_resolvesKnownTemplates() {
        val text1 = "Gift Card for BigBasket code: BB12345678"
        val res1 = parser.parseText(text1)
        assertEquals("BigBasket", res1.merchantName)
        assertEquals("bigbasket", res1.templateKey)

        val text2 = "Myntra Voucher No: MYN88990011"
        val res2 = parser.parseText(text2)
        assertEquals("Myntra", res2.merchantName)
        assertEquals("myntra", res2.templateKey)
    }

    @Test
    fun parseText_emptyInput_returnsZeroConfidence() {
        val result = parser.parseText("")
        assertEquals(0.0f, result.confidenceScore, 0.01f)
        assertNull(result.cardCode)
    }
}
