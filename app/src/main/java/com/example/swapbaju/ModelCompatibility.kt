package com.example.swapbaju

data class ModelCompatibility(
    val compatible: Boolean,
    val reason: String
)

object ModelCompatibility {

    fun validate(
        model: ImportedModel,
        expectedInputWidth: Int? = null,
        expectedInputHeight: Int? = null
    ): ModelCompatibility {

        if (!model.valid) {
            return ModelCompatibility(false, model.error ?: "Model tidak valid.")
        }

        if (model.inputShapes.isEmpty()) {
            return ModelCompatibility(false, "Input tensor tidak ditemukan.")
        }

        if (expectedInputWidth != null && expectedInputHeight != null) {
            val shape = model.inputShapes.first()

            if (shape.size >= 3) {
                val matches = shape.contains(expectedInputWidth) &&
                    shape.contains(expectedInputHeight)

                if (!matches) {
                    return ModelCompatibility(
                        false,
                        "Ukuran input model tidak cocok."
                    )
                }
            }
        }

        return ModelCompatibility(
            true,
            "Struktur tensor dasar terbaca."
        )
    }
}
