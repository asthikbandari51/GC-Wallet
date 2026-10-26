package com.giftvault.app.core.receipts

import android.graphics.BitmapFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ImageProcessorTest {

    private lateinit var imageProcessor: ImageProcessor

    @Before
    fun setUp() {
        imageProcessor = ImageProcessor()
    }

    @Test
    fun `calculateInSampleSize when image is smaller than max dimension returns 1`() {
        val options = BitmapFactory.Options().apply {
            outWidth = 1200
            outHeight = 800
        }

        val sampleSize = imageProcessor.calculateInSampleSize(options, 1600, 1600)

        assertEquals(1, sampleSize)
    }

    @Test
    fun `calculateInSampleSize when image is 3200x2400 returns 2`() {
        val options = BitmapFactory.Options().apply {
            outWidth = 3200
            outHeight = 2400
        }

        val sampleSize = imageProcessor.calculateInSampleSize(options, 1600, 1600)

        assertEquals(2, sampleSize)
    }

    @Test
    fun `calculateInSampleSize when image is 6400x4800 returns 4`() {
        val options = BitmapFactory.Options().apply {
            outWidth = 6400
            outHeight = 4800
        }

        val sampleSize = imageProcessor.calculateInSampleSize(options, 1600, 1600)

        assertEquals(4, sampleSize)
    }

    @Test
    fun `calculateInSampleSize when image is tall 2400x4800 returns 2`() {
        val options = BitmapFactory.Options().apply {
            outWidth = 2400
            outHeight = 4800
        }

        val sampleSize = imageProcessor.calculateInSampleSize(options, 1600, 1600)

        assertEquals(2, sampleSize)
    }
}
