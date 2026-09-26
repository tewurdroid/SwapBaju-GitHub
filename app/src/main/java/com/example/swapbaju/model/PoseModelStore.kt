package com.example.swapbaju.model

import android.content.Context
import java.io.File

data class PoseModelInfo(
    val file: File,
    val sizeBytes: Long
)

object PoseModelStore {
    private const val PREFS = "swapbaju_pose_model"
    private const val KEY_PATH = "path"

    fun save(context: Context, source: File): PoseModelInfo {
        val dir = File(context.filesDir, "models").apply { mkdirs() }
        val target = File(dir, "pose_landmarker_full.task")
        source.inputStream().use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PATH, target.absolutePath)
            .apply()
        return PoseModelInfo(target, target.length())
    }

    fun get(context: Context): PoseModelInfo? {
        val path = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PATH, null)
            ?: return null
        val file = File(path)
        return if (file.isFile && file.length() > 0L) {
            PoseModelInfo(file, file.length())
        } else {
            null
        }
    }

    fun clear(context: Context) {
        val info = get(context)
        info?.file?.delete()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PATH)
            .apply()
    }
}
