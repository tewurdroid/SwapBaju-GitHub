package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker.PoseLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.abs

/**
 * Landmark-guided mesh warp.
 *
 * This is a local/offline geometric warp, not a learned VTON model.
 * It maps a rectangular garment image into a torso mesh defined by
 * shoulder and hip landmarks.
 */
object ClothWarper {

    fun fitCloth(
        cloth: Bitmap,
        person: Bitmap,
        pose: PoseLandmarkerResult
    ): Bitmap {

        if (pose.landmarks().isEmpty()) return cloth

        val lm = pose.landmarks()[0]

        val ls = lm[PoseLandmark.LEFT_SHOULDER]
        val rs = lm[PoseLandmark.RIGHT_SHOULDER]
        val lh = lm[PoseLandmark.LEFT_HIP]
        val rh = lm[PoseLandmark.RIGHT_HIP]

        val leftX = ls.x().coerceIn(0f, 1f) * person.width
        val rightX = rs.x().coerceIn(0f, 1f) * person.width
        val leftHipX = lh.x().coerceIn(0f, 1f) * person.width
        val rightHipX = rh.x().coerceIn(0f, 1f) * person.width

        val shoulderY =
            ((ls.y() + rs.y()) / 2f).coerceIn(0f, 1f) * person.height

        val hipY =
            ((lh.y() + rh.y()) / 2f).coerceIn(0f, 1f) * person.height

        val topWidth = abs(rightX - leftX) * 1.18f
        val bottomWidth = abs(rightHipX - leftHipX) * 1.10f

        val centerTop = (leftX + rightX) / 2f
        val centerBottom = (leftHipX + rightHipX) / 2f

        val top = shoulderY - (hipY - shoulderY) * 0.12f
        val bottom = hipY + (hipY - shoulderY) * 0.08f

        val output = Bitmap.createBitmap(
            person.width,
            person.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val columns = 5
        val rows = 5
        val vertices = FloatArray(columns * rows * 2)

        for (r in 0 until rows) {
            val v = r.toFloat() / (rows - 1)

            // Slightly tapered/expanded torso curve.
            val widthAtRow = topWidth + (bottomWidth - topWidth) * v
            val centerAtRow = centerTop + (centerBottom - centerTop) * v
            val y = top + (bottom - top) * v

            for (c in 0 until columns) {
                val u = c.toFloat() / (columns - 1)

                // Mild nonlinear horizontal shaping.
                val shapedU = u + 0.025f * kotlin.math.sin((u - .5f) * Math.PI).toFloat() * (1f - v)
                val x = centerAtRow + (shapedU - .5f) * widthAtRow

                val i = (r * columns + c) * 2
                vertices[i] = x
                vertices[i + 1] = y
            }
        }

        val clothAspect = cloth.width.toFloat() / cloth.height.toFloat()
        val targetHeight = (bottom - top).coerceAtLeast(1f)
        val targetWidth = (targetHeight * clothAspect).coerceAtLeast(1f)

        val src = FloatArray(columns * rows * 2)

        for (r in 0 until rows) {
            val v = r.toFloat() / (rows - 1)
            for (c in 0 until columns) {
                val u = c.toFloat() / (columns - 1)
                val i = (r * columns + c) * 2
                src[i] = u * cloth.width
                src[i + 1] = v * cloth.height
            }
        }

        // drawBitmapMesh needs destination vertices but its source bitmap
        // retains the original rectangular bounds. Scale source first so
        // the mesh receives a garment with approximately the target aspect.
        val normalized = Bitmap.createScaledBitmap(
            cloth,
            targetWidth.toInt().coerceAtLeast(1),
            targetHeight.toInt().coerceAtLeast(1),
            true
        )

        val left = centerTop - targetWidth / 2f
        val topOffset = top

        val scaledSrc = FloatArray(columns * rows * 2)
        for (r in 0 until rows) {
            val v = r.toFloat() / (rows - 1)
            for (c in 0 until columns) {
                val u = c.toFloat() / (columns - 1)
                val i = (r * columns + c) * 2
                scaledSrc[i] = left + u * targetWidth
                scaledSrc[i + 1] = topOffset + v * targetHeight
            }
        }

        // Convert destination mesh relative to normalized bitmap coordinates.
        val mesh = FloatArray(vertices.size)
        for (i in vertices.indices step 2) {
            mesh[i] = vertices[i] - left
            mesh[i + 1] = vertices[i + 1] - topOffset
        }

        canvas.drawBitmapMesh(
            normalized,
            columns - 1,
            rows - 1,
            mesh,
            0,
            null,
            0,
            paint
        )

        return output
    }
}
