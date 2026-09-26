package com.example.swapbaju

data class ImportedModel(
    val id: String,
    val displayName: String,
    val type: ModelType,
    val uri: String,
    val sizeBytes: Long,
    val inputShapes: List<List<Int>>,
    val outputShapes: List<List<Int>>,
    val valid: Boolean,
    val error: String? = null
)

enum class ModelType {
    HUMAN_PARSING,
    CORRESPONDENCE,
    REFINEMENT
}
