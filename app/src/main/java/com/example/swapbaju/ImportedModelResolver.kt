package com.example.swapbaju

import android.content.Context
import java.io.File

data class ResolvedModel(
    val metadata: ImportedModel,
    val localFile: File
)

object ImportedModelResolver {

    fun resolve(
        context: Context,
        type: ModelType
    ): ResolvedModel? {
        val activeId = ActiveModelStore.getActiveId(context, type)
            ?: return null

        val metadata = ModelImportStore.load(context)
            .firstOrNull { it.id == activeId && it.valid }
            ?: return null

        val file = ModelImporter.copyIntoPrivateStorage(
            context,
            metadata
        ) ?: return null

        return ResolvedModel(metadata, file)
    }
}
