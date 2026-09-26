package com.example.swapbaju.model

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Resamples the garment using a dense field where each target pixel stores
 * a normalized source (x,y) coordinate and confidence.
 *
 * This is a conservative backward warp: invalid/low-confidence samples remain
 * transparent instead of inventing pixels.
 */
object DenseFieldWarper {

    data class Result(
        val bitmap: Bitmap,
        val mask: Bitmap
    )

    fun warp(
        garment: Bitmap,
        garmentMask: Bitmap?,
        field: CorrespondenceField,
        targetWidth: Int,
        targetHeight: Int,
        confidenceThreshold: Float = 0.15f
    ): Result {
        val out = Bitmap.createBitmap(
            targetWidth,
            targetHeight,
            Bitmap.Config.ARGB_8888
        )
        val mask = Bitmap.createBitmap(
            targetWidth,
            targetHeight,
            Bitmap.Config.ALPHA_8
        )

        val garmentPixels = IntArray(garment.width * garment.height)
        garment.getPixels(
            garmentPixels, 0, garment.width, 0, 0,
            garment.width, garment.height
        )

        val maskPixels = garmentMask?.let {
            ByteArray(it.width * it.height).also { bytes ->
                val tmp = IntArray(it.width * it.height)
                it.getPixels(tmp, 0, it.width, 0, 0, it.width, it.height)
                for (i in tmp.indices) bytes[i] = ((tmp[i] ushr 24) and 0xFF).toByte()
            }
        }

        val outPixels = IntArray(targetWidth * targetHeight)
        val outMask = ByteArray(targetWidth * targetHeight)

        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val fx = toFieldX(x, targetWidth, field.width)
                val fy = toFieldY(y, targetHeight, field.height)
                val fieldIndex = fieldIndex(
                    fx, fy, field.width, field.height
                )

                val confidence = field.confidence[fieldIndex].coerceIn(0f, 1f)
                if (confidence < confidenceThreshold) continue

                val sx = (field.xMap[fieldIndex].coerceIn(0f, 1f) * (garment.width - 1))
                    .roundToInt()
                val sy = (field.yMap[fieldIndex].coerceIn(0f, 1f) * (garment.height - 1))
                    .roundToInt()

                if (sx !in 0 until garment.width || sy !in 0 until garment.height) continue

                val sourceIndex = sy * garment.width + sx
                val alpha = if (maskPixels != null) {
                    val mx = (sx.toFloat() / max(1, garment.width - 1) *
                        max(1, garmentMask!!.width - 1)).roundToInt()
                    val my = (sy.toFloat() / max(1, garment.height - 1) *
                        max(1, garmentMask.height - 1)).roundToInt()
                    maskPixels[
                        my.coerceIn(0, garmentMask.height - 1) * garmentMask.width +
                            mx.coerceIn(0, garmentMask.width - 1)
                    ].toInt() and 0xFF
                } else {
                    (garmentPixels[sourceIndex] ushr 24) and 0xFF
                }

                if (alpha <= 0) continue

                val adjustedAlpha = (alpha * confidence).roundToInt().coerceIn(0, 255)
                val color = garmentPixels[sourceIndex]
                outPixels[y * targetWidth + x] =
                    (color and 0x00FFFFFF) or (adjustedAlpha shl 24)
                outMask[y * targetWidth + x] = adjustedAlpha.toByte()
            }
        }

        out.setPixels(
            outPixels, 0, targetWidth, 0, 0,
            targetWidth, targetHeight
        )
        mask.copyPixelsFromBuffer(java.nio.ByteBuffer.wrap(outMask))
        return Result(out, mask)
    }

    private fun toFieldX(value: Int, target: Int, field: Int): Int {
        if (field <= 1 || target <= 1) return 0
        return (value.toFloat() / (target - 1) * (field - 1))
            .roundToInt().coerceIn(0, field - 1)
    }

    private fun toFieldY(value: Int, target: Int, field: Int): Int {
        if (field <= 1 || target <= 1) return 0
        return (value.toFloat() / (target - 1) * (field - 1))
            .roundToInt().coerceIn(0, field - 1)
    }

    private fun fieldIndex(x: Int, y: Int, width: Int, height: Int): Int =
        y.coerceIn(0, height - 1) * width + x.coerceIn(0, width - 1)
}
