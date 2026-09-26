package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Color

object SegmentationMaskBuilder {

    fun buildGarmentMask(
        labels: IntArray,
        width: Int,
        height: Int,
        garmentClasses: Set<Int>
    ): Bitmap {
        require(labels.size == width * height)

        val pixels = IntArray(labels.size)
        for (i in labels.indices) {
            pixels[i] = if (labels[i] in garmentClasses) 255 else 0
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8).also {
            it.setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    fun buildPersonMask(
        labels: IntArray,
        width: Int,
        height: Int,
        backgroundClass: Int
    ): Bitmap {
        require(labels.size == width * height)

        val pixels = IntArray(labels.size)
        for (i in labels.indices) {
            pixels[i] = if (labels[i] == backgroundClass) 0 else 255
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8).also {
            it.setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
