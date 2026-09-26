package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object GarmentPreprocessor {

    data class Result(
        val bitmap: Bitmap,
        val mask: Bitmap
    )

    fun prepare(input: Bitmap): Result {
        val src = input.copy(Bitmap.Config.ARGB_8888, true)
        val w = src.width
        val h = src.height

        val samples = intArrayOf(
            src.getPixel(0, 0),
            src.getPixel(w - 1, 0),
            src.getPixel(0, h - 1),
            src.getPixel(w - 1, h - 1)
        )

        val bgR = samples.map(Color::red).average()
        val bgG = samples.map(Color::green).average()
        val bgB = samples.map(Color::blue).average()

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val mask = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        val alphaPixels = IntArray(w * h)

        for (i in pixels.indices) {
            val c = pixels[i]
            val d = abs(Color.red(c) - bgR) +
                    abs(Color.green(c) - bgG) +
                    abs(Color.blue(c) - bgB)

            val a = when {
                d < 42.0 -> 0
                d < 70.0 -> ((d - 42.0) / 28.0 * 255.0).toInt()
                else -> 255
            }.coerceIn(0, 255)

            alphaPixels[i] = a
            pixels[i] = Color.argb(a, Color.red(c), Color.green(c), Color.blue(c))
        }

        src.setPixels(pixels, 0, w, 0, 0, w, h)
        mask.setPixels(alphaPixels, 0, w, 0, 0, w, h)

        return Result(src, mask)
    }
}
