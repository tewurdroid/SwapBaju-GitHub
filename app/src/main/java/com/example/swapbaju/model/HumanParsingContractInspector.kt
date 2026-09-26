package com.example.swapbaju.model

import org.tensorflow.lite.Interpreter
import java.io.File

data class HumanParsingContractSuggestion(
    val inputWidth: Int?,
    val inputHeight: Int?,
    val outputClasses: Int?,
    val outputLayout: String?,
    val notes: List<String>
)

object HumanParsingContractInspector {

    fun inspect(file: File): HumanParsingContractSuggestion {
        val notes = mutableListOf<String>()
        return runCatching {
            val interpreter = Interpreter(file)
            val input = interpreter.getInputTensor(0).shape()
            val output = interpreter.getOutputTensor(0).shape()

            val inputWidth: Int?
            val inputHeight: Int?
            if (input.size == 4 && input[0] == 1 && input[3] == 3) {
                inputHeight = input[1]
                inputWidth = input[2]
                notes += "Input RGB [1,H,W,3] terdeteksi."
            } else {
                inputWidth = null
                inputHeight = null
                notes += "Input bukan kontrak RGB [1,H,W,3]."
            }

            val classes: Int?
            val layout: String?
            if (output.size == 4 && inputHeight != null && inputWidth != null &&
                output[1] == inputHeight && output[2] == inputWidth
            ) {
                classes = output[3]
                layout = "[1,H,W,C]"
                notes += "Output per-pixel [1,H,W,C] terdeteksi."
            } else if (output.size == 4 && inputHeight != null && inputWidth != null &&
                output[2] == inputHeight && output[3] == inputWidth
            ) {
                classes = output[1]
                layout = "[1,C,H,W]"
                notes += "Output channel-first [1,C,H,W] terdeteksi; layout ini didukung runtime v2.0."
            } else {
                classes = null
                layout = null
                notes += "Bentuk output belum dikenali sebagai segmentasi per-pixel."
            }

            interpreter.close()
            HumanParsingContractSuggestion(
                inputWidth = inputWidth,
                inputHeight = inputHeight,
                outputClasses = classes,
                outputLayout = layout,
                notes = notes
            )
        }.getOrElse {
            HumanParsingContractSuggestion(
                inputWidth = null,
                inputHeight = null,
                outputClasses = null,
                outputLayout = null,
                notes = listOf("Gagal membaca model: ${it.message ?: "unknown error"}")
            )
        }
    }
}
