package com.example.swapbaju.model

import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

/**
 * Common image refinement contract:
 * input: RGB image [1,H,W,3]
 * output: RGB image [1,H,W,3] or [1,3,H,W]
 *
 * Input/output ranges are configurable. This adapter does not assume
 * a particular neural architecture.
 */
class FileTfliteImageRefiner(
    modelFile: File,
    private val config: RefinementRuntimeConfig
) : ImageRefiner {

    private val interpreter = Interpreter(modelFile)

    init {
        require(modelFile.exists() && modelFile.length() > 0L)
        require(config.outputChannels == 3) {
            "v2.3 refinement adapter currently expects 3 output channels."
        }

        val input = interpreter.getInputTensor(0).shape()
        require(input.contentEquals(intArrayOf(1, config.inputHeight, config.inputWidth, 3))) {
            "Refiner input must be [1,H,W,3]. Got ${input.contentToString()}"
        }

        val output = interpreter.getOutputTensor(0).shape()
        val hwc = output.contentEquals(
            intArrayOf(1, config.inputHeight, config.inputWidth, 3)
        )
        val chw = output.contentEquals(
            intArrayOf(1, 3, config.inputHeight, config.inputWidth)
        )
        require(hwc || chw) {
            "Refiner output must be [1,H,W,3] or [1,3,H,W]. Got ${output.contentToString()}"
        }
    }

    override fun refine(input: RefinementInput): Bitmap {
        val scaled = Bitmap.createScaledBitmap(
            input.composite,
            config.inputWidth,
            config.inputHeight,
            true
        )

        val buffer = ByteBuffer.allocateDirect(
            config.inputWidth * config.inputHeight * 3 * 4
        ).order(ByteOrder.nativeOrder())

        val pixels = IntArray(config.inputWidth * config.inputHeight)
        scaled.getPixels(
            pixels, 0, config.inputWidth, 0, 0,
            config.inputWidth, config.inputHeight
        )

        val range = config.inputMax - config.inputMin
        require(range > 0f) { "Invalid refinement input range." }

        for (p in pixels) {
            buffer.putFloat(config.inputMin + (((p shr 16) and 255) / 255f) * range)
            buffer.putFloat(config.inputMin + (((p shr 8) and 255) / 255f) * range)
            buffer.putFloat(config.inputMin + ((p and 255) / 255f) * range)
        }
        buffer.rewind()

        val outputShape = interpreter.getOutputTensor(0).shape()
        val hwc = outputShape[1] == config.inputHeight &&
            outputShape[2] == config.inputWidth

        val hwcOut = Array(config.inputHeight) {
            Array(config.inputWidth) { FloatArray(3) }
        }
        val chwOut = Array(3) {
            Array(config.inputHeight * config.inputWidth) { FloatArray(1) }
        }

        if (hwc) {
            interpreter.run(buffer, hwcOut)
        } else {
            interpreter.run(buffer, chwOut)
        }

        val outputPixels = IntArray(config.inputWidth * config.inputHeight)
        val outRange = config.outputMax - config.outputMin
        require(outRange > 0f) { "Invalid refinement output range." }

        for (y in 0 until config.inputHeight) {
            for (x in 0 until config.inputWidth) {
                val idx = y * config.inputWidth + x
                val r = if (hwc) hwcOut[y][x][0] else chwOut[0][idx][0]
                val g = if (hwc) hwcOut[y][x][1] else chwOut[1][idx][0]
                val b = if (hwc) hwcOut[y][x][2] else chwOut[2][idx][0]

                val rr = toByte(r, config.outputMin, outRange)
                val gg = toByte(g, config.outputMin, outRange)
                val bb = toByte(b, config.outputMin, outRange)

                // Preserve the composited alpha at this stage.
                val a = (pixels[idx] ushr 24) and 255
                outputPixels[idx] =
                    (a shl 24) or (rr shl 16) or (gg shl 8) or bb
            }
        }

        val refined = Bitmap.createBitmap(
            config.inputWidth,
            config.inputHeight,
            Bitmap.Config.ARGB_8888
        )
        refined.setPixels(
            outputPixels, 0, config.inputWidth,
            0, 0, config.inputWidth, config.inputHeight
        )

        scaled.recycle()

        return Bitmap.createScaledBitmap(
            refined,
            input.composite.width,
            input.composite.height,
            true
        ).also {
            refined.recycle()
        }
    }

    fun close() = interpreter.close()

    private fun toByte(v: Float, min: Float, range: Float): Int =
        (((v - min) / range).coerceIn(0f, 1f) * 255f).roundToInt()
}
