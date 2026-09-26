package com.example.swapbaju

import android.graphics.Bitmap

data class CorrespondenceField(
    val width: Int,
    val height: Int,
    val xMap: FloatArray,
    val yMap: FloatArray,
    val confidence: FloatArray
) {
    init {
        require(xMap.size == width * height)
        require(yMap.size == width * height)
        require(confidence.size == width * height)
    }

    fun index(x: Int, y: Int): Int = y * width + x
}

interface DenseCorrespondenceEngine {
    fun estimate(
        person: Bitmap,
        garment: Bitmap,
        humanParsing: HumanParsingResult
    ): CorrespondenceField?
}
