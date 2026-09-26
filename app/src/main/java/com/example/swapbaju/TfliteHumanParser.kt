package com.example.swapbaju

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TfliteHumanParser(
    context: Context,
    private val config: HumanParsingModelConfig
) : HumanParser {

    private val interpreter: Interpreter

    init {
        val bytes = context.assets.open(config.assetName).use { it.readBytes() }
        val buffer = ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
        buffer.put(bytes)
        buffer.rewind()
        interpreter = Interpreter(buffer)
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
            pixels, 0, config.inputWidth,
            0, 0, config.inputWidth, config.inputHeight
        )

        for (p in pixels) {
            val rgb = floatArrayOf(
                ((p shr 16) and 0xff) / 255f,
                ((p shr 8) and 0xff) / 255f,
                (p and 0xff) / 255f
            )
            input.putFloat((rgb[0] - config.mean[0]) / config.std[0])
            input.putFloat((rgb[1] - config.mean[1]) / config.std[1])
            input.putFloat((rgb[2] - config.mean[2]) / config.std[2])
        }
        input.rewind()

        val output = Array(config.inputHeight) {
            Array(config.inputWidth) {
                FloatArray(config.outputClasses)
            }
        }

        // Expected model output:
        // [H][W][classes].
        interpreter.run(input, output)

        val labels = IntArray(config.inputWidth * config.inputHeight)
        var confidenceSum = 0f

        for (y in 0 until config.inputHeight) {
            for (x in 0 until config.inputWidth) {
                val scores = output[y][x]
                var bestClass = 0
                var bestScore = Float.NEGATIVE_INFINITY

                for (c in scores.indices) {
                    if (scores[c] > bestScore) {
                        bestScore = scores[c]
                        bestClass = c
                    }
                }

                labels[y * config.inputWidth + x] =
                    if (bestScore >= config.confidenceThreshold) bestClass
                    else config.backgroundClass

                confidenceSum += bestScore.coerceIn(0f, 1f)
            }
        }

        val garmentClasses =
            config.upperGarmentClasses + config.lowerGarmentClasses

        val garmentMask = SegmentationMaskBuilder.buildGarmentMask(
            labels,
            config.inputWidth,
            config.inputHeight,
            garmentClasses
        )

        val personMask = SegmentationMaskBuilder.buildPersonMask(
            labels,
            config.inputWidth,
            config.inputHeight,
            config.backgroundClass
        )

        return HumanParsingResult(
            personMask = personMask,
            garmentRegionMask = garmentMask,
            confidence = confidenceSum / labels.size
        )
    }

    fun close() = interpreter.close()
}
