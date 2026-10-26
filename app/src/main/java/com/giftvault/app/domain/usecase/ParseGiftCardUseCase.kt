package com.giftvault.app.domain.usecase

import android.content.Context
import android.net.Uri
import com.giftvault.app.core.ocr.MlKitOcrEngine
import com.giftvault.app.core.parser.GiftCardTextParser
import com.giftvault.app.domain.model.GiftCardParseResult
import com.giftvault.app.domain.model.ParseSourceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ParseGiftCardUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrEngine: MlKitOcrEngine,
    private val textParser: GiftCardTextParser,
    private val checkDuplicateCardUseCase: CheckDuplicateCardUseCase
) {
    suspend fun parseFromImage(imageUri: Uri): GiftCardParseResult = withContext(Dispatchers.IO) {
        val extractedText = ocrEngine.extractTextFromImage(context, imageUri)
        val result = textParser.parseText(
            rawText = extractedText,
            sourceType = ParseSourceType.IMAGE,
            imageUri = imageUri
        )
        return@withContext checkDuplicate(result)
    }

    suspend fun parseFromText(
        text: String,
        sourceType: ParseSourceType = ParseSourceType.PASTED_TEXT
    ): GiftCardParseResult = withContext(Dispatchers.IO) {
        val result = textParser.parseText(
            rawText = text,
            sourceType = sourceType,
            imageUri = null
        )
        return@withContext checkDuplicate(result)
    }

    private suspend fun checkDuplicate(result: GiftCardParseResult): GiftCardParseResult {
        val code = result.cardCode ?: return result
        val existingCard = checkDuplicateCardUseCase(code, result.merchantName)
        return if (existingCard != null) {
            result.copy(
                isDuplicate = true,
                duplicateCardId = existingCard.id
            )
        } else {
            result
        }
    }
}
