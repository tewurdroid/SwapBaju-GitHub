package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

class AlphaVtonRenderer : VtonRenderer {

    override fun render(
        person: Bitmap,
        warpedGarment: Bitmap,
        garmentMask: Bitmap?,
        bodyMask: Bitmap?
    ): Bitmap {
        val result = person.copy(Bitmap.Config.ARGB_8888, true)

        val effectiveMask = when {
            garmentMask != null && bodyMask != null ->
                MaskUtils.multiply(garmentMask, bodyMask)
            garmentMask != null -> garmentMask
            else -> bodyMask
        }

        if (effectiveMask == null) {
            Canvas(result).drawBitmap(
                warpedGarment,
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG)
            )
            return result
        }

        val alphaGarment = MaskUtils.applyAlpha(
            warpedGarment,
            effectiveMask
        )

        Canvas(result).drawBitmap(
            alphaGarment,
            0f,
            0f,
            Paint(Paint.ANTI_ALIAS_FLAG)
        )

        return result
    }
}
