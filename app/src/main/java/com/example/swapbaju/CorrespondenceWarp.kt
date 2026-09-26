package com.example.swapbaju

import android.graphics.Bitmap
import kotlin.math.floor

object CorrespondenceWarp {

    fun warp(
        source: Bitmap,
        field: CorrespondenceField
    ): Bitmap {
        val out = Bitmap.createBitmap(
            field.width,
            field.height,
            Bitmap.Config.ARGB_8888
        )

        val pixels = IntArray(field.width * field.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)

        for (y in 0 until field.height) {
            for (x in 0 until field.width) {
                val i = field.index(x, y)
                val sx = floor(field.xMap[i]).toInt().coerceIn(0, source.width - 1)
                val sy = floor(field.yMap[i]).toInt().coerceIn(0, source.height - 1)

                out.setPixel(x, y, source.getPixel(sx, sy))
            }
        }

        return out
    }
}
