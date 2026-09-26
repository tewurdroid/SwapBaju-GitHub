package com.example.swapbaju

data class ModelCompatibilityResult(
    val compatible: Boolean,
    val reason: String
)

object ModelCompatibility {

    fun validate(
        model: ImportedModel,
        expectedInputWidth: Int? = null,
        expectedInputHeight: Int? = null
    ): ModelCompatibilityResult {

        if (!model.valid) {
            return ModelCompatibilityResult(false, model.error ?: "Model tidak valid.")
        }

        if (model.inputShapes.isEmpty()) {
            return ModelCompatibilityResult(false, "Input tensor tidak ditemukan.")
        }

        if (expectedInputWidth != null && expectedInputHeight != null) {
            val shape = model.inputShapes.first()

            if (shape.size >= 3) {
                val matches = shape.contains(expectedInputWidth) &&
                    shape.contains(expectedInputHeight)

                if (!matches) {
                    return ModelCompatibilityResult(
                        false,
                        "Ukuran input model tidak cocok."
                    )
                }
            }
        }

        return ModelCompatibilityResult(
            true,
            "Struktur tensor dasar terbaca."
        )
    }
}
