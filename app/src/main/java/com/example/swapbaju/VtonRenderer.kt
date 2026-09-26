package com.example.swapbaju

import android.graphics.Bitmap

interface VtonRenderer {
    fun render(
        person: Bitmap,
        warpedGarment: Bitmap,
        garmentMask: Bitmap?,
        bodyMask: Bitmap?
    ): Bitmap
}
