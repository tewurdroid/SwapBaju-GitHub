package com.example.swapbaju

import android.graphics.Bitmap

interface SegmentationEngine {
    fun segment(person: Bitmap): SegmentationResult
    fun close() {}
}
