package com.example.swapbaju

import android.content.Context
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

object VtonPipelineFactory {

    fun create(
        context: Context,
        pose: PoseLandmarkerResult,
        requestedMode: VtonMode
    ): VtonPipelineBundle {

        val availability = ModelAvailabilityChecker.check(context)

        val actualMode = when (requestedMode) {
            VtonMode.FULL_VTON ->
                if (availability.hasFullVton) VtonMode.FULL_VTON
                else VtonMode.FALLBACK

            VtonMode.AI_HUMAN_PARSING ->
                if (availability.hasAiParsing) VtonMode.AI_HUMAN_PARSING
                else VtonMode.FALLBACK

            VtonMode.FALLBACK -> VtonMode.FALLBACK
        }

        val parser: HumanParser =
            when (actualMode) {
                VtonMode.FALLBACK ->
                    PoseHumanParser(pose)

                VtonMode.AI_HUMAN_PARSING ->
                    // The exact tensor/class configuration must be supplied
                    // for the selected model. Keep the safe fallback until
                    // that contract is explicitly configured.
                    PoseHumanParser(pose)

                VtonMode.FULL_VTON ->
                    PoseHumanParser(pose)
            }

        val correspondence: DenseCorrespondenceEngine =
            PoseCorrespondenceEngine(pose)

        val refiner: ImageRefiner =
            IdentityImageRefiner()

        return VtonPipelineBundle(
            mode = actualMode,
            pipeline = RefinedVtonPipeline(
                parser = parser,
                correspondence = correspondence,
                renderer = AlphaVtonRenderer(),
                refiner = refiner
            )
        )
    }
}

data class VtonPipelineBundle(
    val mode: VtonMode,
    val pipeline: RefinedVtonPipeline
)
