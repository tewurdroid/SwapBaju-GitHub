package com.example.swapbaju.model


import com.example.swapbaju.*
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Common dense correspondence contract:
 * input 0 = person RGB [1,H,W,3]
 * input 1 = garment RGB [1,H,W,3]
 * output = [1,H,W,3] or [1,3,H,W], channels x/y/confidence.
 */
class FileTfliteCorrespondenceEngine(
    modelFile: File,
    private val config: CorrespondenceRuntimeConfig
) : DenseCorrespondenceEngine {

    private val interpreter = Interpreter(modelFile)

    init {
        require(modelFile.exists() && modelFile.length() > 0L)
        require(interpreter.inputTensorCount >= 2) {
            "Correspondence model requires at least two inputs."
        }
        val a = interpreter.getInputTensor(0).shape()
        val b = interpreter.getInputTensor(1).shape()
        require(a.contentEquals(intArrayOf(1, config.inputHeight, config.inputWidth, 3))) {
            "Person input must be [1,H,W,3]. Got ${a.contentToString()}"
        }
        require(b.contentEquals(intArrayOf(1, config.inputHeight, config.inputWidth, 3))) {
            "Garment input must be [1,H,W,3]. Got ${b.contentToString()}"
        }
        val o = interpreter.getOutputTensor(0).shape()
        val hwc = o.contentEquals(intArrayOf(1, config.outputHeight, config.outputWidth, 3))
        val chw = o.contentEquals(intArrayOf(1, 3, config.outputHeight, config.outputWidth))
        require(hwc || chw) {
            "Output must be [1,H,W,3] or [1,3,H,W]. Got ${o.contentToString()}"
        }
    }

    override fun estimate(
        person: Bitmap,
        garment: Bitmap,
        humanParsing: HumanParsingResult
    ): CorrespondenceField {
        val personInput = rgbBuffer(person)
        val garmentInput = rgbBuffer(garment)
        val o = interpreter.getOutputTensor(0).shape()
        val hwc = o[1] == config.outputHeight &&
            o[2] == config.outputWidth && o[3] == 3

        val hwcOut = Array(config.outputHeight) {
            Array(config.outputWidth) { FloatArray(3) }
        }
        val chwOut = Array(3) {
            Array(config.outputHeight * config.outputWidth) { FloatArray(1) }
        }

        if (hwc) {
            interpreter.runForMultipleInputsOutputs(
                arrayOf(personInput, garmentInput),
                mapOf(0 to hwcOut)
            )
        } else {
            interpreter.runForMultipleInputsOutputs(
                arrayOf(personInput, garmentInput),
                mapOf(0 to chwOut)
            )
        }

        val n = config.outputWidth * config.outputHeight
        val x = FloatArray(n)
        val y = FloatArray(n)
        val c = FloatArray(n)

        for (yy in 0 until config.outputHeight) {
            for (xx in 0 until config.outputWidth) {
                val idx = yy * config.outputWidth + xx
                val rx = if (hwc) hwcOut[yy][xx][0] else chwOut[0][idx][0]
                val ry = if (hwc) hwcOut[yy][xx][1] else chwOut[1][idx][0]
                val rc = if (hwc) hwcOut[yy][xx][2] else chwOut[2][idx][0]
                x[idx] = normalize(rx, config.outputXMin, config.outputXMax)
                y[idx] = normalize(ry, config.outputYMin, config.outputYMax)
                c[idx] = normalize(rc, config.confidenceMin, config.confidenceMax)
            }
        }

        return CorrespondenceField(config.outputWidth, config.outputHeight, x, y, c)
    }

    fun close() = interpreter.close()

    private fun rgbBuffer(bitmap: Bitmap): ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(
            bitmap, config.inputWidth, config.inputHeight, true
        )
        val buffer = ByteBuffer.allocateDirect(
            config.inputWidth * config.inputHeight * 3 * 4
        ).order(ByteOrder.nativeOrder())
        val pixels = IntArray(config.inputWidth * config.inputHeight)
        scaled.getPixels(pixels, 0, config.inputWidth, 0, 0,
            config.inputWidth, config.inputHeight)
        val range = config.inputRangeMax - config.inputRangeMin
        require(range > 0f)
        for (p in pixels) {
            buffer.putFloat(config.inputRangeMin + (((p shr 16) and 255) / 255f) * range)
            buffer.putFloat(config.inputRangeMin + (((p shr 8) and 255) / 255f) * range)
            buffer.putFloat(config.inputRangeMin + ((p and 255) / 255f) * range)
        }
        buffer.rewind()
        scaled.recycle()
        return buffer
    }

    private fun normalize(v: Float, lo: Float, hi: Float): Float {
        val d = hi - lo
        return if (d == 0f) 0f else ((v - lo) / d).coerceIn(0f, 1f)
    }
}
