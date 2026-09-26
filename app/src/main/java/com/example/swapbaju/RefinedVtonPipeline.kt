package com.example.swapbaju

import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class RefinedVtonPipeline(
    private val parser: HumanParser,
    private val correspondence: DenseCorrespondenceEngine,
    private val renderer: VtonRenderer = AlphaVtonRenderer(),
    private val refiner: ImageRefiner = IdentityImageRefiner()
) {
    fun run(
        person: Bitmap,
        garment: Bitmap,
        garmentSourceMask: Bitmap?,
        pose: PoseLandmarkerResult
    ): Bitmap {
        val parsing = parser.parse(person)

        correspondence.estimate(
            person,
            garment,
            parsing
        )

        val targetGarmentMask = parsing.garmentRegionMask

        val warped = ClothingAwareWarp.fit(
            garment = garment,
            garmentMask = garmentSourceMask ?: targetGarmentMask,
            person = person,
            pose = pose
        )

        val composite = renderer.render(
            person = person,
            warpedGarment = warped.bitmap,
            garmentMask = warped.mask,
            bodyMask = parsing.personMask
        )

        return refiner.refine(
            RefinementInput(
                person = person,
                composite = composite,
                garmentMask = warped.mask,
                bodyMask = parsing.personMask
            )
        )
    }
}
