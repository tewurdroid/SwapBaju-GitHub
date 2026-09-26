package com.example.swapbaju.model

import android.content.Context

data class PoseRuntimeStatus(
    val source: String,
    val sizeBytes: Long,
    val usable: Boolean,
    val message: String
)

object PoseRuntimeStatusChecker {
    private const val ASSET = "pose_landmarker_full.task"

    fun check(context: Context): PoseRuntimeStatus {
        val imported = PoseModelStore.get(context)
        if (imported != null) {
            return PoseRuntimeStatus(
                "Imported private model",
                imported.sizeBytes,
                imported.sizeBytes > 0L,
                "Pose Landmarker model terpasang dari penyimpanan aplikasi (${imported.sizeBytes} bytes)."
            )
        }

        return runCatching {
            context.assets.open(ASSET).use { input ->
                val size = input.readBytes().size.toLong()
                if (size <= 0L) {
                    PoseRuntimeStatus(
                        "Bundled asset",
                        size,
                        false,
                        "Pose asset ditemukan tetapi kosong."
                    )
                } else {
                    PoseRuntimeStatus(
                        "Bundled asset",
                        size,
                        true,
                        "Pose Landmarker asset tersedia (${size} bytes)."
                    )
                }
            }
        }.getOrElse {
            PoseRuntimeStatus(
                "Unavailable",
                0L,
                false,
                "Model pose belum dipasang. Import file pose_landmarker_full.task terlebih dahulu."
            )
        }
    }
}
