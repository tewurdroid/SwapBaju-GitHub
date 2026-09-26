package com.example.swapbaju.model


import com.example.swapbaju.*
import android.content.Context

data class VtonResolvedMode(
    val mode: VtonMode,
    val reason: String,
    val humanReady: Boolean,
    val correspondenceReady: Boolean,
    val refinementReady: Boolean
)

object VtonModeResolver {

    fun resolve(context: Context): VtonResolvedMode {
        val human = humanReady(context)
        val correspondence = correspondenceReady(context)
        val refinement = refinementReady(context)

        return when {
            human && correspondence && refinement ->
                VtonResolvedMode(
                    VtonMode.FULL_VTON,
                    "Human Parsing + Dense Correspondence + Refinement siap.",
                    true, true, true
                )

            human ->
                VtonResolvedMode(
                    VtonMode.AI_HUMAN_PARSING,
                    "Human Parsing siap; komponen VTON penuh belum lengkap.",
                    true, correspondence, refinement
                )

            else ->
                VtonResolvedMode(
                    VtonMode.FALLBACK,
                    "Model Human Parsing belum siap; memakai fallback pose.",
                    false, correspondence, refinement
                )
        }
    }

    private fun humanReady(context: Context): Boolean =
        runCatching {
            val active = ActiveModelStore.getActive(context, ModelType.HUMAN_PARSING)
                ?: return false
            val config = HumanParsingRuntimeConfigStore.load(context)
                ?: return false
            val resolved = ImportedModelResolver.resolve(context, active)
            FileTfliteHumanParser(
                resolved.localFile,
                config.toModelConfig(resolved.metadata.displayName)
            ).also { it.close() }
            true
        }.getOrDefault(false)

    private fun correspondenceReady(context: Context): Boolean =
        runCatching {
            val active = ActiveModelStore.getActive(context, ModelType.CORRESPONDENCE)
                ?: return false
            val config = CorrespondenceRuntimeConfigStore.load(context)
                ?: return false
            val resolved = ImportedModelResolver.resolve(context, active)
            FileTfliteCorrespondenceEngine(resolved.localFile, config).also { it.close() }
            true
        }.getOrDefault(false)

    private fun refinementReady(context: Context): Boolean =
        runCatching {
            val active = ActiveModelStore.getActive(context, ModelType.REFINEMENT)
                ?: return false
            val config = RefinementRuntimeConfigStore.load(context)
                ?: return false
            val resolved = ImportedModelResolver.resolve(context, active)
            FileTfliteImageRefiner(resolved.localFile, config).also { it.close() }
            true
        }.getOrDefault(false)
}
