package com.example.swapbaju.model

import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

/**
 * Human parser backed by a TFLite model stored in app-private storage.
 *
 * The model's tensor contract is intentionally supplied by HumanParsingModelConfig.
 * This prevents guessing class meanings for arbitrary imported segmentation models.
 */
class FileTfliteHumanParser(
    private val modelFile: File,
    private val config: HumanParsingModelConfig
) : HumanParser {

    private val interpreter: Interpreter

    init {
        require(modelFile.exists() && modelFile.length() > 0L) {
            "Human parsing model file is missing or empty."
        }
        val options = Interpreter.Options()
        interpreter = Interpreter(modelFile, options)
        validateContract()
    }

    private fun validateContract() {
        require(interpreter.inputTensorCount >= 1) {
            "Human parsing model has no input tensor."
        }
        require(interpreter.outputTensorCount >= 1) {
            "Human parsing model has no output tensor."
        }

        val input = interpreter.getInputTensor(0).shape()
        require(input.size == 4 && input[0] == 1 && input[3] == 3) {
            "Unsupported human parsing input shape: ${input.contentToString()}. Expected [1,H,W,3]."
        }
        require(input[1] == config.inputHeight && input[2] == config.inputWidth) {
            "Config ${config.inputWidth}x${config.inputHeight} does not match model ${input[2]}x${input[1]}."
        }

        val output = interpreter.getOutputTensor(0).shape()
        require(output.size == 4 && output[0] == 1) {
            "Unsupported human parsing output shape: ${output.contentToString()}."
        }
        val hwc =
            output[1] == config.inputHeight &&
            output[2] == config.inputWidth &&
            output[3] == config.outputClasses

        val chw =
            output[1] == config.outputClasses &&
            output[2] == config.inputHeight &&
            output[3] == config.inputWidth

        require(hwc || chw) {
            "Unsupported output shape ${output.contentToString()}. Expected [1,H,W,C] or [1,C,H,W]."
        }
    }

    override fun parse(person: Bitmap): HumanParsingResult {
        val resized = Bitmap.createScaledBitmap(
            person,
            config.inputWidth,
            config.inputHeight,
            true
        )

        val input = ByteBuffer.allocateDirect(
            config.inputWidth * config.inputHeight * 3 * 4
        ).order(ByteOrder.nativeOrder())

        val pixels = IntArray(config.inputWidth * config.inputHeight)
        resized.getPixels(
            pixels, 0, config.inputWidth, 0, 0,
            config.inputWidth, config.inputHeight
        )

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            input.putFloat((r - config.mean[0]) / config.std[0])
            input.putFloat((g - config.mean[1]) / config.std[1])
            input.putFloat((b - config.mean[2]) / config.std[2])
        }
        input.rewind()

        val outputShape = interpreter.getOutputTensor(0).shape()
        val isHwc =
            outputShape[1] == config.inputHeight &&
            outputShape[2] == config.inputWidth &&
            outputShape[3] == config.outputClasses

        val output = if (isHwc) {
            Array(config.inputHeight) {
                Array(config.inputWidth) { FloatArray(config.outputClasses) }
            }
        } else {
            Array(config.outputClasses) {
                Array(config.inputHeight * config.inputWidth) { FloatArray(1) }
            }
        }

        interpreter.run(input, arrayOf(output))

        val garment = Bitmap.createBitmap(
            config.inputWidth,
            config.inputHeight,
            Bitmap.Config.ALPHA_8
        )
        val personMask = Bitmap.createBitmap(
            config.inputWidth,
            config.inputHeight,
            Bitmap.Config.ALPHA_8
        )

        val garmentPixels = ByteArray(config.inputWidth * config.inputHeight)
        val personPixels = ByteArray(config.inputWidth * config.inputHeight)

        var confident = 0
        var foreground = 0

        for (y in 0 until config.inputHeight) {
            for (x in 0 until config.inputWidth) {
                var bestClass = 0
                var bestScore = Float.NEGATIVE_INFINITY
                for (c in 0 until config.outputClasses) {
                    val score = if (isHwc) {
                        output[y][x][c]
                    } else {
                        output[c][y * config.inputWidth + x][0]
                    }
                    if (score > bestScore) {
                        bestScore = score
                        bestClass = c
                    }
                }

                val index = y * config.inputWidth + x
                val ok = bestScore >= config.confidenceThreshold
                if (ok) confident++

                if (bestClass != config.backgroundClass && ok) {
                    personPixels[index] = 255.toByte()
                    foreground++
                }
                if (bestClass in config.upperGarmentClasses && ok) {
                    garmentPixels[index] = 255.toByte()
                }
                if (bestClass in config.lowerGarmentClasses && ok) {
                    garmentPixels[index] = 255.toByte()
                }
            }
        }

        garment.copyPixelsFromBuffer(ByteBuffer.wrap(garmentPixels))
        personMask.copyPixelsFromBuffer(ByteBuffer.wrap(personPixels))

        resized.recycle()

        val confidence = if (config.inputWidth * config.inputHeight == 0) {
            0f
        } else {
            confident.toFloat() / (config.inputWidth * config.inputHeight).toFloat()
        }

        // If no garment classes were explicitly configured, keep the garment
        // mask null rather than pretending arbitrary classes mean clothing.
        val hasGarmentClasses =
            config.upperGarmentClasses.isNotEmpty() || config.lowerGarmentClasses.isNotEmpty()

        return HumanParsingResult(
            personMask = personMask,
            garmentRegionMask = if (hasGarmentClasses && foreground > 0) garment else null,
            confidence = confidence
        )
    }

    fun close() {
        interpreter.close()
    }
}
