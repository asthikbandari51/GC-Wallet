package com.giftvault.app.core.parser

import android.net.Uri
import com.giftvault.app.core.templates.TemplateRegistry
import com.giftvault.app.domain.model.GiftCardParseResult
import com.giftvault.app.domain.model.ParseSourceType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiftCardTextParser @Inject constructor() {

    fun parseText(
        rawText: String,
        sourceType: ParseSourceType = ParseSourceType.PASTED_TEXT,
        imageUri: Uri? = null
    ): GiftCardParseResult {
        if (rawText.isBlank()) {
            return GiftCardParseResult(
                sourceType = sourceType,
                confidenceScore = 0.0f,
                rawText = rawText,
                imageUri = imageUri
            )
        }

        val normalizedText = normalizeText(rawText)
        val lines = normalizedText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var code: String? = null
        var pin: String? = null
        var issueDateStr: String? = null
        var expiryDateStr: String? = null
        var merchantStr: String? = null
        var serviceStr: String? = null
        var detailsStr: String? = null
        var amountVal: Double? = null

        val fieldConfidences = mutableMapOf<String, Float>()

        // 1. Process key-value pairs (checking current line or next line for values)
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val lowerLine = line.lowercase()

            // Check for Combined Code/Pin line first: e.g. "9802004325817067/pin:898443"
            val combinedMatch = ParsePatterns.COMBINED_CODE_PIN_REGEX.find(line)
            if (combinedMatch != null && code == null) {
                code = combinedMatch.groupValues[1]
                pin = combinedMatch.groupValues[2]
                fieldConfidences["cardCode"] = 0.95f
                fieldConfidences["pin"] = 0.95f
                i++
                continue
            }

            // Code key matching
            if (code == null && ParsePatterns.CODE_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") || lowerLine.contains("$it -") }) {
                val valueOnSameLine = extractValueFromKeyLine(line, ParsePatterns.CODE_KEYS)
                if (valueOnSameLine.isNotBlank()) {
                    val extracted = cleanCodeValue(valueOnSameLine)
                    if (extracted.first.isNotBlank()) {
                        code = extracted.first
                        fieldConfidences["cardCode"] = 0.9f
                    }
                    if (extracted.second != null && pin == null) {
                        pin = extracted.second
                        fieldConfidences["pin"] = 0.9f
                    }
                } else if (i + 1 < lines.size) {
                    val nextLine = lines[i + 1]
                    val extracted = cleanCodeValue(nextLine)
                    if (extracted.first.isNotBlank()) {
                        code = extracted.first
                        fieldConfidences["cardCode"] = 0.85f
                        i++
                    }
                    if (extracted.second != null && pin == null) {
                        pin = extracted.second
                        fieldConfidences["pin"] = 0.85f
                    }
                }
                i++
                continue
            }

            // PIN key matching
            if (pin == null && ParsePatterns.PIN_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") || lowerLine.contains("$it -") }) {
                val valueOnSameLine = extractValueFromKeyLine(line, ParsePatterns.PIN_KEYS)
                if (valueOnSameLine.isNotBlank()) {
                    pin = cleanPinValue(valueOnSameLine)
                    fieldConfidences["pin"] = 0.9f
                } else if (i + 1 < lines.size) {
                    val nextLine = lines[i + 1]
                    pin = cleanPinValue(nextLine)
                    fieldConfidences["pin"] = 0.85f
                    i++
                }
                i++
                continue
            }

            // Issue Date key matching
            if (issueDateStr == null && ParsePatterns.ISSUE_DATE_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") }) {
                val valSame = extractValueFromKeyLine(line, ParsePatterns.ISSUE_DATE_KEYS)
                if (valSame.isNotBlank()) {
                    issueDateStr = valSame
                    fieldConfidences["issueDate"] = 0.9f
                } else if (i + 1 < lines.size) {
                    issueDateStr = lines[i + 1]
                    fieldConfidences["issueDate"] = 0.85f
                    i++
                }
                i++
                continue
            }

            // Expiry Date key matching
            if (expiryDateStr == null && ParsePatterns.EXPIRY_DATE_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") }) {
                val valSame = extractValueFromKeyLine(line, ParsePatterns.EXPIRY_DATE_KEYS)
                if (valSame.isNotBlank()) {
                    expiryDateStr = valSame
                    fieldConfidences["expiryDate"] = 0.9f
                } else if (i + 1 < lines.size) {
                    expiryDateStr = lines[i + 1]
                    fieldConfidences["expiryDate"] = 0.85f
                    i++
                }
                i++
                continue
            }

            // Merchant / Service Provider key matching
            if (merchantStr == null && ParsePatterns.MERCHANT_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") }) {
                val valSame = extractValueFromKeyLine(line, ParsePatterns.MERCHANT_KEYS)
                if (valSame.isNotBlank()) {
                    merchantStr = valSame
                    fieldConfidences["merchantName"] = 0.9f
                } else if (i + 1 < lines.size) {
                    merchantStr = lines[i + 1]
                    fieldConfidences["merchantName"] = 0.85f
                    i++
                }
                i++
                continue
            }

            // Service key matching
            if (serviceStr == null && ParsePatterns.SERVICE_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") }) {
                val valSame = extractValueFromKeyLine(line, ParsePatterns.SERVICE_KEYS)
                if (valSame.isNotBlank()) {
                    serviceStr = valSame
                    fieldConfidences["service"] = 0.85f
                } else if (i + 1 < lines.size) {
                    serviceStr = lines[i + 1]
                    fieldConfidences["service"] = 0.80f
                    i++
                }
                i++
                continue
            }

            // Service/Product Details key matching
            if (detailsStr == null && ParsePatterns.DETAILS_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") }) {
                val valSame = extractValueFromKeyLine(line, ParsePatterns.DETAILS_KEYS)
                if (valSame.isNotBlank()) {
                    detailsStr = valSame
                    fieldConfidences["details"] = 0.85f
                } else if (i + 1 < lines.size) {
                    detailsStr = lines[i + 1]
                    fieldConfidences["details"] = 0.80f
                    i++
                }
                i++
                continue
            }

            // Amount key matching
            if (amountVal == null && ParsePatterns.AMOUNT_KEYS.any { lowerLine.startsWith(it) || lowerLine.contains("$it:") }) {
                val valSame = extractValueFromKeyLine(line, ParsePatterns.AMOUNT_KEYS)
                val parsedAmt = ParsePatterns.parseAmount(valSame)
                if (parsedAmt != null) {
                    amountVal = parsedAmt
                    fieldConfidences["amount"] = 0.9f
                } else if (i + 1 < lines.size) {
                    amountVal = ParsePatterns.parseAmount(lines[i + 1])
                    if (amountVal != null) {
                        fieldConfidences["amount"] = 0.85f
                        i++
                    }
                }
                i++
                continue
            }

            i++
        }

        // 2. Fallback RegEx scanning for unparsed fields
        if (code == null) {
            for (line in lines) {
                val match = ParsePatterns.STANDALONE_CODE_REGEX.find(line)
                if (match != null && match.value.length >= 10 && !match.value.all { it.isDigit() && it == '0' }) {
                    code = match.value
                    fieldConfidences["cardCode"] = 0.6f
                    break
                }
            }
        }

        if (amountVal == null) {
            // Try extracting amount from details or raw text
            amountVal = ParsePatterns.parseAmount(detailsStr ?: rawText)
            if (amountVal != null) {
                fieldConfidences["amount"] = 0.7f
            }
        }

        // Parse Date strings to LocalDate
        val parsedIssueDate = issueDateStr?.let { ParsePatterns.parseDate(it) }
        val parsedExpiryDate = expiryDateStr?.let { ParsePatterns.parseDate(it) }

        // Merchant detection & template matching
        var matchedTemplateKey: String? = null
        val merchantToMatch = merchantStr ?: detailsStr ?: serviceStr ?: ""
        if (merchantToMatch.isNotBlank()) {
            val template = TemplateRegistry.getTemplateForMerchant(merchantToMatch)
                ?: findMerchantInRegistry(merchantToMatch)
            if (template != null) {
                matchedTemplateKey = template.templateKey
                if (merchantStr == null) {
                    merchantStr = template.displayName
                    fieldConfidences["merchantName"] = 0.75f
                } else {
                    fieldConfidences["merchantName"] = fieldConfidences["merchantName"] ?: 0.85f
                }
                fieldConfidences["templateKey"] = 0.9f
            } else {
                matchedTemplateKey = "other"
                fieldConfidences["templateKey"] = 0.5f
            }
        } else {
            // Search all lines for registered merchant names
            for (line in lines) {
                val template = TemplateRegistry.getTemplateForMerchant(line) ?: findMerchantInRegistry(line)
                if (template != null) {
                    merchantStr = template.displayName
                    matchedTemplateKey = template.templateKey
                    fieldConfidences["merchantName"] = 0.8f
                    fieldConfidences["templateKey"] = 0.85f
                    break
                }
            }
        }

        // 3. Compute overall confidence score
        val scores = mutableListOf<Float>()
        fieldConfidences["cardCode"]?.let { scores.add(it) }
        fieldConfidences["merchantName"]?.let { scores.add(it) }
        fieldConfidences["amount"]?.let { scores.add(it) }

        val overallConfidence = if (scores.isNotEmpty()) {
            scores.average().toFloat()
        } else {
            0.2f
        }

        return GiftCardParseResult(
            merchantName = merchantStr?.trim(),
            templateKey = matchedTemplateKey ?: "other",
            cardCode = code?.trim(),
            pin = pin?.trim(),
            amount = amountVal,
            issueDate = parsedIssueDate,
            expiryDate = parsedExpiryDate,
            service = serviceStr?.trim(),
            details = detailsStr?.trim(),
            sourceType = sourceType,
            confidenceScore = overallConfidence,
            fieldConfidences = fieldConfidences,
            rawText = rawText,
            imageUri = imageUri
        )
    }

    private fun normalizeText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(Regex("""[ \t]+"""), " ")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    private fun extractValueFromKeyLine(line: String, keys: List<String>): String {
        val lowerLine = line.lowercase()
        val keyFound = keys.firstOrNull { lowerLine.contains(it) } ?: return ""
        val index = lowerLine.indexOf(keyFound) + keyFound.length
        var remaining = line.substring(index).trim()
        if (remaining.startsWith(":") || remaining.startsWith("-") || remaining.startsWith("=")) {
            remaining = remaining.substring(1).trim()
        }
        return remaining
    }

    private fun cleanCodeValue(rawVal: String): Pair<String, String?> {
        val combinedMatch = ParsePatterns.COMBINED_CODE_PIN_REGEX.find(rawVal)
        if (combinedMatch != null) {
            return Pair(combinedMatch.groupValues[1], combinedMatch.groupValues[2])
        }

        // Check if string contains "pin:" or "/pin:" or "pin-"
        if (rawVal.contains("/pin:", ignoreCase = true) || rawVal.contains("pin:", ignoreCase = true)) {
            val parts = rawVal.split(Regex("""/pin:|pin:""", RegexOption.IGNORE_CASE))
            val codePart = parts[0].trim().replace(Regex("""[^A-Za-z0-9]"""), "")
            val pinPart = if (parts.size > 1) parts[1].trim().replace(Regex("""[^A-Za-z0-9]"""), "") else null
            return Pair(codePart, pinPart)
        }

        val cleanCode = rawVal.trim().replace(" ", "").replace("-", "")
        return Pair(cleanCode, null)
    }

    private fun cleanPinValue(rawVal: String): String {
        return rawVal.trim().replace(Regex("""[^0-9A-Za-z]"""), "")
    }

    private fun findMerchantInRegistry(text: String): com.giftvault.app.core.templates.TemplateManifest? {
        val clean = text.lowercase()
        for (template in TemplateRegistry.getAllTemplates()) {
            if (template.templateKey == "other") continue
            val nameLower = template.displayName.lowercase()
            val keyLower = template.templateKey.lowercase()
            if (clean.contains(nameLower) || clean.contains(keyLower)) {
                return template
            }
        }
        return null
    }
}
