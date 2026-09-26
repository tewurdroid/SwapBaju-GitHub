package com.example.swapbaju

import android.content.Context

data class ModelAvailability(
    val pose: Boolean,
    val humanParsing: Boolean,
    val correspondence: Boolean,
    val refinement: Boolean
) {
    val hasAiParsing: Boolean
        get() = humanParsing

    val hasFullVton: Boolean
        get() = humanParsing && correspondence && refinement
}

object ModelAvailabilityChecker {

    fun check(context: Context): ModelAvailability {
        fun exists(name: String): Boolean =
            try {
                context.assets.open(name).use { it.available() > 0 }
            } catch (_: Exception) {
                false
            }

        return ModelAvailability(
            pose = exists("pose_landmarker_full.task"),
            humanParsing = exists("models/human_parsing.tflite"),
            correspondence = exists("models/vton_correspondence.tflite"),
            refinement = exists("models/vton_refiner.tflite")
        )
    }

    fun bestAvailable(context: Context): VtonMode {
        val a = check(context)

        return when {
            a.hasFullVton -> VtonMode.FULL_VTON
            a.hasAiParsing -> VtonMode.AI_HUMAN_PARSING
            else -> VtonMode.FALLBACK
        }
    }
}
