package com.example.swapbaju

data class HumanParsingModelConfig(
    val assetName: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val mean: FloatArray = floatArrayOf(0f, 0f, 0f),
    val std: FloatArray = floatArrayOf(1f, 1f, 1f),
    val outputClasses: Int,
    val upperGarmentClasses: Set<Int>,
    val lowerGarmentClasses: Set<Int> = emptySet(),
    val skinClasses: Set<Int> = emptySet(),
    val hairClasses: Set<Int> = emptySet(),
    val backgroundClass: Int = 0,
    val confidenceThreshold: Float = 0.5f
)
