package com.example.swapbaju

import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseHumanParser(
    private val pose: PoseLandmarkerResult
) : HumanParser {

    override fun parse(person: Bitmap): HumanParsingResult {
        val mask = OcclusionMask.createGarmentMask(person, pose)
        return HumanParsingResult(
            personMask = mask,
            garmentRegionMask = mask,
            confidence = 0.25f
        )
    }
}
