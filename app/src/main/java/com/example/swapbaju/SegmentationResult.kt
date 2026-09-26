package com.example.swapbaju

import android.graphics.Bitmap

data class SegmentationResult(
    val personMask: Bitmap?,
    val clothingMask: Bitmap?
)
