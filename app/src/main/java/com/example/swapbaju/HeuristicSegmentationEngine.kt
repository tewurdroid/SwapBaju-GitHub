package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Safe fallback used until a dedicated local segmentation .task model is installed.
 *
 * It creates a conservative torso/person mask from pose geometry.
 * It does not classify clothing pixels.
 */
class HeuristicSegmentationEngine(
    private val pose: PoseLandmarkerResult
) : SegmentationEngine {

    override fun segment(person: Bitmap): SegmentationResult {

        if (pose.landmarks().isEmpty()) {
            return SegmentationResult(null, null)
        }

        val lm = pose.landmarks()[0]

        fun px(index: Int): Float =
            lm[index].x().coerceIn(0f, 1f) * person.width

        fun py(index: Int): Float =
            lm[index].y().coerceIn(0f, 1f) * person.height

        val path = Path()

        path.moveTo(
            px(PoseLandmark.LEFT_SHOULDER),
            py(PoseLandmark.LEFT_SHOULDER)
        )

        path.lineTo(
            px(PoseLandmark.RIGHT_SHOULDER),
            py(PoseLandmark.RIGHT_SHOULDER)
        )

        path.lineTo(
            px(PoseLandmark.RIGHT_HIP),
            py(PoseLandmark.RIGHT_HIP)
        )

        path.lineTo(
            px(PoseLandmark.LEFT_HIP),
            py(PoseLandmark.LEFT_HIP)
        )

        path.close()

        val mask = Bitmap.createBitmap(
            person.width,
            person.height,
            Bitmap.Config.ALPHA_8
        )

        Canvas(mask).drawPath(
            path,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                alpha = 255
            }
        )

        return SegmentationResult(
            personMask = mask,
            clothingMask = null
        )
    }
}
