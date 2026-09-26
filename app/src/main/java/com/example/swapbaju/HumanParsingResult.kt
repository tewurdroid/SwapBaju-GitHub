package com.example.swapbaju

import android.graphics.Bitmap

data class HumanParsingResult(
    val personMask: Bitmap?,
    val garmentRegionMask: Bitmap?,
    val confidence: Float
)
