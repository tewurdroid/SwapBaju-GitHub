package com.example.swapbaju

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ModelManager {

    fun inspect(context: Context, assetName: String): ModelInfo {
        return try {
            val bytes = context.assets.open(assetName).use { it.readBytes() }

            if (bytes.isEmpty()) {
                return ModelInfo(
                    assetName = assetName,
                    exists = true,
                    sizeBytes = 0,
                    inputShapes = emptyList(),
                    outputShapes = emptyList(),
                    error = "File model kosong."
                )
            }

            val buffer = ByteBuffer.allocateDirect(bytes.size)
                .order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()

            val interpreter = Interpreter(buffer)

            val inputs = (0 until interpreter.inputTensorCount)
                .map { interpreter.getInputTensor(it).shape().toList() }

            val outputs = (0 until interpreter.outputTensorCount)
                .map { interpreter.getOutputTensor(it).shape().toList() }

            interpreter.close()

            ModelInfo(
                assetName = assetName,
                exists = true,
                sizeBytes = bytes.size.toLong(),
                inputShapes = inputs,
                outputShapes = outputs
            )
        } catch (e: Exception) {
            ModelInfo(
                assetName = assetName,
                exists = false,
                sizeBytes = 0,
                inputShapes = emptyList(),
                outputShapes = emptyList(),
                error = e.message ?: "Gagal membaca model."
            )
        }
    }

    fun inspectAll(context: Context): List<ModelInfo> {
        return listOf(
            "models/human_parsing.tflite",
            "models/vton_correspondence.tflite",
            "models/vton_refiner.tflite"
        ).map { inspect(context, it) }
    }

    fun summary(context: Context): String {
        return inspectAll(context).joinToString("\n\n") { info ->
            buildString {
                append(info.assetName)
                append(": ")
                append(
                    when {
                        !info.exists -> "TIDAK ADA"
                        !info.usable -> "TIDAK VALID"
                        else -> "TERSEDIA"
                    }
                )

                if (info.sizeBytes > 0) {
                    append(" (${info.sizeBytes / 1024} KB)")
                }

                if (info.inputShapes.isNotEmpty()) {
                    append("\nInput: ")
                    append(info.inputShapes.joinToString())
                }

                if (info.outputShapes.isNotEmpty()) {
                    append("\nOutput: ")
                    append(info.outputShapes.joinToString())
                }

                info.error?.let {
                    append("\nError: ")
                    append(it)
                }
            }
        }
    }
}
