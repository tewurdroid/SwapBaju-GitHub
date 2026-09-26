package com.example.swapbaju.model


import com.example.swapbaju.*
import android.content.Context
import java.io.File

data class ResolvedPoseModel(
    val source: String,
    val file: File?
)

object PoseModelResolver {
    private const val ASSET = "pose_landmarker_full.task"

    fun resolve(context: Context): ResolvedPoseModel {
        val imported = PoseModelStore.get(context)
        if (imported != null) {
            return ResolvedPoseModel("Imported private model", imported.file)
        }

        val bundled = runCatching {
            context.assets.open(ASSET).use { it.available() > 0 }
        }.getOrDefault(false)

        return if (bundled) {
            ResolvedPoseModel("Bundled asset", null)
        } else {
            ResolvedPoseModel("Unavailable", null)
        }
    }
}
