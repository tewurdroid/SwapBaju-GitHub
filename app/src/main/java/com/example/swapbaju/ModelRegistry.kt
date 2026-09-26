package com.example.swapbaju

import android.content.Context

object ModelRegistry {

    fun createHumanParser(
        context: Context,
        poseFallback: HumanParser
    ): HumanParser {
        return try {
            context.assets.open("models/human_parsing.tflite").close()

            // Tensor dimensions/class count are model-specific.
            // Keep fallback until the selected model's documented contract
            // has been configured.
            poseFallback
        } catch (_: Exception) {
            poseFallback
        }
    }
}
