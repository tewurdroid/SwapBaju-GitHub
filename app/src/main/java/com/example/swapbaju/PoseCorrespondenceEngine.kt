package com.example.swapbaju

import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseCorrespondenceEngine(
    private val pose: PoseLandmarkerResult
) : DenseCorrespondenceEngine {

    override fun estimate(
        person: Bitmap,
        garment: Bitmap,
        humanParsing: HumanParsingResult
    ): CorrespondenceField? {
        if (pose.landmarks().isEmpty()) return null

        val w = garment.width
        val h = garment.height
        val x = FloatArray(w * h)
        val y = FloatArray(w * h)
        val confidence = FloatArray(w * h) { 0.25f }

        // Identity correspondence is intentionally used as a safe fallback.
        // The actual garment placement is still handled by ClothWarper.
        for (yy in 0 until h) {
            for (xx in 0 until w) {
                val i = yy * w + xx
                x[i] = xx.toFloat()
                y[i] = yy.toFloat()
            }
        }

        return CorrespondenceField(w, h, x, y, confidence)
    }
}
