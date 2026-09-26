package com.example.swapbaju.model


import com.example.swapbaju.*
import android.content.Context
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

object VtonPipelineFactory {

    data class BuildResult(
        val pipeline: RefinedVtonPipeline,
        val mode: VtonMode,
        val status: String
    )

    fun create(context: Context, pose: PoseLandmarkerResult, requestedMode: VtonMode = VtonModeResolver.resolve(context).mode): BuildResult {
        val resolvedMode = VtonModeResolver.resolve(context)

        var parser: HumanParser = PoseHumanParser(pose)
        var parserStatus = "Fallback pose parser aktif."

        val activeHuman = ActiveModelStore.getActive(context, ModelType.HUMAN_PARSING)
        if (activeHuman != null) {
            val resolved = runCatching {
                ImportedModelResolver.resolve(context, activeHuman)
            }.getOrNull()

            if (resolved != null) {
                val runtime = HumanParsingRuntimeConfigStore.load(context)
                val config = runtime?.toModelConfig(resolved.metadata.displayName)
                    ?: HumanParsingConfigLoader.load(context, "models/human_parsing_config.json")

                if (config != null) {
                    val configLooksIntentional =
                        config.upperGarmentClasses.isNotEmpty() ||
                        config.lowerGarmentClasses.isNotEmpty()

                    if (configLooksIntentional) {
                        parser = FileTfliteHumanParser(resolved.localFile, config)
                        parserStatus = "Human parsing aktif: ${resolved.metadata.displayName}"
                    } else {
                        parserStatus =
                            "Model ditemukan, tetapi class mapping belum dikonfigurasi."
                    }
                } else {
                    parserStatus = "Konfigurasi human parsing tidak valid."
                }
            } else {
                parserStatus = "Model human parsing aktif tidak dapat dibuka."
            }
        } else if (requestedMode == VtonMode.AI_HUMAN_PARSING ||
            requestedMode == VtonMode.FULL_VTON
        ) {
            parserStatus = "Tidak ada human parsing model aktif; memakai fallback."
        }

        var correspondence: DenseCorrespondenceEngine = PoseCorrespondenceEngine(pose)
        if (requestedMode == VtonMode.FULL_VTON) {
            val active = ActiveModelStore.getActive(context, ModelType.CORRESPONDENCE)
            val config = CorrespondenceRuntimeConfigStore.load(context)
            if (active != null && config != null) {
                val resolved = runCatching {
                    ImportedModelResolver.resolve(context, active)
                }.getOrNull()
                if (resolved != null) {
                    correspondence = runCatching {
                        FileTfliteCorrespondenceEngine(resolved.localFile, config)
                    }.getOrElse { PoseCorrespondenceEngine(pose) }
                }
            }
        }
        var refiner: ImageRefiner = IdentityImageRefiner()
        if (requestedMode == VtonMode.FULL_VTON) {
            val activeRefiner = ActiveModelStore.getActive(context, ModelType.REFINEMENT)
            val config = RefinementRuntimeConfigStore.load(context)
            if (activeRefiner != null && config != null) {
                val resolved = runCatching {
                    ImportedModelResolver.resolve(context, activeRefiner)
                }.getOrNull()
                if (resolved != null) {
                    refiner = runCatching {
                        FileTfliteImageRefiner(resolved.localFile, config)
                    }.getOrElse { IdentityImageRefiner() }
                }
            }
        }

        val pipeline = RefinedVtonPipeline(
            parser = parser,
            correspondence = correspondence,
            renderer = AlphaVtonRenderer(),
            refiner = refiner
        )

        return BuildResult(
            pipeline = pipeline,
            mode = requestedMode,
            status = "${resolvedMode.reason} ${parserStatus}"
        )
    }
}
