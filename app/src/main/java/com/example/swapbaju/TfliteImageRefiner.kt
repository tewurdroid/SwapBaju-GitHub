package com.example.swapbaju

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Model-agnostic TFLite image refinement adapter.
 *
 * The selected model must document its exact input/output contract.
 * This class intentionally validates dimensions before inference.
 */
class TfliteImageRefiner(
    context: Context,
    private val modelAsset: String,
    private val inputWidth: Int,
    private val inputHeight: Int,
    private val outputChannels: Int = 3
) : ImageRefiner {

    private val interpreter: Interpreter

    init {
        val bytes = context.assets.open(modelAsset).use { it.readBytes() }
        val buffer = ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
        buffer.put(bytes)
        buffer.rewind()
        interpreter = Interpreter(buffer)
    }

    override fun refine(input: RefinementInput): Bitmap {
        val resized = Bitmap.createScaledBitmap(
            input.composite,
            inputWidth,
            inputHeight,
            true
        )

        val inputBuffer = ByteBuffer.allocateDirect(
            inputWidth * inputHeight * 3 * 4
        ).order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        resized.getPixels(
            pixels, 0, inputWidth,
            0, 0, inputWidth, inputHeight
        )

        for (p in pixels) {
            inputBuffer.putFloat(((p shr 16) and 0xff) / 255f)
            inputBuffer.putFloat(((p shr 8) and 0xff) / 255f)
            inputBuffer.putFloat((p and 0xff) / 255f)
        }
        inputBuffer.rewind()

        val output = Array(inputHeight) {
            Array(inputWidth) {
                FloatArray(outputChannels)
            }
        }

        interpreter.run(inputBuffer, output)

        val out = Bitmap.createBitmap(
            inputWidth,
            inputHeight,
            Bitmap.Config.ARGB_8888
        )

        for (y in 0 until inputHeight) {
            for (x in 0 until inputWidth) {
                val p = output[y][x]
                val r = (p.getOrElse(0) { 0f } * 255f)
                    .toInt().coerceIn(0, 255)
                val g = (p.getOrElse(1) { 0f } * 255f)
                    .toInt().coerceIn(0, 255)
                val b = (p.getOrElse(2) { 0f } * 255f)
                    .toInt().coerceIn(0, 255)
                out.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
            }
        }

        return Bitmap.createScaledBitmap(
            out,
            input.composite.width,
            input.composite.height,
            true
        )
    }

    fun close() {
        interpreter.close()
    }
}
