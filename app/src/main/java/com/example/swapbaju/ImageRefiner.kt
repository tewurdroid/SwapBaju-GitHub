package com.example.swapbaju

import android.graphics.Bitmap

interface ImageRefiner {
    fun refine(input: RefinementInput): Bitmap
}
