package com.example.swapbaju

data class ModelInfo(
    val assetName: String,
    val exists: Boolean,
    val sizeBytes: Long,
    val inputShapes: List<List<Int>>,
    val outputShapes: List<List<Int>>,
    val error: String? = null
) {
    val usable: Boolean
        get() = exists && error == null && sizeBytes > 0
}
