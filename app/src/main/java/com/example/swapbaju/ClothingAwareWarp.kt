package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Combines the existing landmark warp with an optional AI garment mask.
 *
 * The AI mask is treated as a visibility constraint, not as a replacement
 * for geometric placement.
 */
object ClothingAwareWarp {

    data class Result(
        val bitmap: Bitmap,
        val mask: Bitmap?
    )

    fun fit(
        garment: Bitmap,
        garmentMask: Bitmap?,
        person: Bitmap,
        pose: PoseLandmarkerResult
    ): Result {
        val warped = ClothWarper.fitCloth(garment, person, pose)

        if (garmentMask == null) {
            return Result(warped, null)
        }

        val resizedMask = MaskUtils.resizeMask(
            garmentMask,
            warped.width,
            warped.height
        )

        val feathered = MaskUtils.feather(
            resizedMask,
            (warped.width * 0.008f).coerceAtLeast(1f)
        )

        return Result(
            MaskUtils.applyAlpha(warped, feathered),
            feathered
        )
    }
}
