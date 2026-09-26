package com.example.swapbaju.model

import android.content.Context

data class VtonRuntimeReadiness(
    val mode: VtonMode,
    val ready: Boolean,
    val humanParsing: String,
    val correspondence: String,
    val refinement: String
) {
    fun summary(): String = buildString {
        appendLine("Mode: $mode")
        appendLine("Human Parsing: $humanParsing")
        appendLine("Dense Correspondence: $correspondence")
        appendLine("Image Refinement: $refinement")
        append("Status: ${if (ready) "READY" else "FALLBACK / PERLU KONFIGURASI"}")
    }
}

object VtonRuntimeReadinessChecker {

    fun check(context: Context): VtonRuntimeReadiness {
        val mode = ModelAvailabilityChecker.bestAvailable(context)

        val human = checkHuman(context)
        val correspondence = checkCorrespondence(context)
        val refinement = checkRefinement(context)

        val ready = human.first &&
            (mode != VtonMode.FULL_VTON || correspondence.first) &&
            (mode != VtonMode.FULL_VTON || refinement.first)

        return VtonRuntimeReadiness(
            mode = mode,
            ready = ready,
            humanParsing = human.second,
            correspondence = correspondence.second,
            refinement = refinement.second
        )
    }

    private fun checkHuman(context: Context): Pair<Boolean, String> {
        val active = ActiveModelStore.getActive(context, ModelType.HUMAN_PARSING)
            ?: return false to "Belum ada model aktif."

        val config = HumanParsingRuntimeConfigStore.load(context)
            ?: return false to "Konfigurasi belum disimpan."

        val resolved = runCatching {
            ImportedModelResolver.resolve(context, active)
        }.getOrNull() ?: return false to "File model tidak dapat dibuka."

        return runCatching {
            FileTfliteHumanParser(
                resolved.localFile,
                config.toModelConfig(resolved.metadata.displayName)
            )
        }.fold(
            onSuccess = { parser ->
                parser.close()
                true to "OK — ${resolved.metadata.displayName}"
            },
            onFailure = { false to "Tidak kompatibel: ${it.message ?: "unknown"}" }
        )
    }

    private fun checkCorrespondence(context: Context): Pair<Boolean, String> {
        val active = ActiveModelStore.getActive(context, ModelType.CORRESPONDENCE)
            ?: return false to "Belum ada model aktif."

        val config = CorrespondenceRuntimeConfigStore.load(context)
            ?: return false to "Konfigurasi belum disimpan."

        val resolved = runCatching {
            ImportedModelResolver.resolve(context, active)
        }.getOrNull() ?: return false to "File model tidak dapat dibuka."

        return runCatching {
            FileTfliteCorrespondenceEngine(resolved.localFile, config)
        }.fold(
            onSuccess = { engine ->
                engine.close()
                true to "OK — ${resolved.metadata.displayName}"
            },
            onFailure = { false to "Tidak kompatibel: ${it.message ?: "unknown"}" }
        )
    }

    private fun checkRefinement(context: Context): Pair<Boolean, String> {
        val active = ActiveModelStore.getActive(context, ModelType.REFINEMENT)
            ?: return false to "Belum ada model aktif."

        val config = RefinementRuntimeConfigStore.load(context)
            ?: return false to "Konfigurasi belum disimpan."

        val resolved = runCatching {
            ImportedModelResolver.resolve(context, active)
        }.getOrNull() ?: return false to "File model tidak dapat dibuka."

        return runCatching {
            FileTfliteImageRefiner(resolved.localFile, config)
        }.fold(
            onSuccess = { refiner ->
                refiner.close()
                true to "OK — ${resolved.metadata.displayName}"
            },
            onFailure = { false to "Tidak kompatibel: ${it.message ?: "unknown"}" }
        )
    }
}
