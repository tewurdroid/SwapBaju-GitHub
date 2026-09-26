package com.example.swapbaju

import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class VtonPipeline(
    private val parser: HumanParser,
    private val correspondence: DenseCorrespondenceEngine,
    private val renderer: VtonRenderer = AlphaVtonRenderer()
) {
    fun run(
        person: Bitmap,
        garment: Bitmap,
        pose: PoseLandmarkerResult
    ): Bitmap {
        val parsing = parser.parse(person)

        correspondence.estimate(
            person,
            garment,
            parsing
        )

        val warped = ClothingAwareWarp.fit(
            garment = garment,
            garmentMask = parsing.garmentRegionMask,
            person = person,
            pose = pose
        )

        return renderer.render(
            person = person,
            warpedGarment = warped.bitmap,
            garmentMask = warped.mask,
            bodyMask = parsing.personMask
        )
    }
}
