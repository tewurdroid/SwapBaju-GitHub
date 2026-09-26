package com.example.swapbaju

import android.graphics.Bitmap

data class RefinementInput(
    val person: Bitmap,
    val composite: Bitmap,
    val garmentMask: Bitmap?,
    val bodyMask: Bitmap?
)
