package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max

object OcclusionMask {

    fun createGarmentMask(
        person: Bitmap,
        pose: PoseLandmarkerResult
    ): Bitmap? {
        if (pose.landmarks().isEmpty()) return null
        val lm = pose.landmarks()[0]

        fun x(i: Int) = lm[i].x().coerceIn(0f, 1f) * person.width
        fun y(i: Int) = lm[i].y().coerceIn(0f, 1f) * person.height

        val mask = Bitmap.createBitmap(
            person.width, person.height, Bitmap.Config.ALPHA_8
        )
        val canvas = Canvas(mask)

        val allow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 255
            style = Paint.Style.FILL
        }

        val torso = Path().apply {
            moveTo(x(PoseLandmark.LEFT_SHOULDER), y(PoseLandmark.LEFT_SHOULDER))
            lineTo(x(PoseLandmark.RIGHT_SHOULDER), y(PoseLandmark.RIGHT_SHOULDER))
            lineTo(x(PoseLandmark.RIGHT_HIP), y(PoseLandmark.RIGHT_HIP))
            lineTo(x(PoseLandmark.LEFT_HIP), y(PoseLandmark.LEFT_HIP))
            close()
        }
        canvas.drawPath(torso, allow)

        val block = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = max(18f, person.width * 0.06f)
        }

        fun limb(a: Int, b: Int) {
            canvas.drawLine(x(a), y(a), x(b), y(b), block)
        }

        limb(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        limb(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        limb(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        limb(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

        // Small neck exclusion corridor.
        block.strokeWidth = max(14f, person.width * 0.045f)
        limb(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)

        return mask
    }
}
