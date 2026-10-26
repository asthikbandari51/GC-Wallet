package com.giftvault.app.core.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MlKitOcrEngine @Inject constructor() {

    suspend fun extractTextFromImage(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        val processedBitmap = ImagePreprocessUtils.getProcessedBitmap(context, imageUri)
        val inputImage = InputImage.fromBitmap(processedBitmap, 0)
        
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        if (continuation.isActive) {
                            continuation.resume(visionText.text)
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
        } finally {
            recognizer.close()
            if (!processedBitmap.isRecycled) {
                processedBitmap.recycle()
            }
        }
    }
}
