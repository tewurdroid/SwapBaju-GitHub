package com.example.swapbaju

import android.content.Context
import java.nio.ByteBuffer
import org.tensorflow.lite.Interpreter

data class ModelValidation(
    val valid: Boolean,
    val message: String,
    val inputShape: List<Int> = emptyList(),
    val outputShapes: List<List<Int>> = emptyList()
)

object ModelValidator {

    fun validate(
        context: Context,
        assetName: String
    ): ModelValidation {
        return try {
            val bytes = context.assets.open(assetName).use { it.readBytes() }
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.rewind()

            val interpreter = Interpreter(buffer)
            val input = interpreter.getInputTensor(0).shape().toList()
            val outputs = (0 until interpreter.outputTensorCount)
                .map { interpreter.getOutputTensor(it).shape().toList() }
            interpreter.close()

            ModelValidation(
                valid = true,
                message = "Model loaded successfully.",
                inputShape = input,
                outputShapes = outputs
            )
        } catch (e: Exception) {
            ModelValidation(
                valid = false,
                message = e.message ?: "Unknown model error."
            )
        }
    }
}
