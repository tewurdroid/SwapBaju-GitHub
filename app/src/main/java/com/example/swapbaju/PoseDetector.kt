package com.example.swapbaju

import android.content.Context
import android.graphics.Bitmap
import com.example.swapbaju.model.PoseModelStore
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val MODEL_ASSET = "pose_landmarker_full.task"

class PoseDetector(context: Context) {

    private fun createBaseOptions(context: Context): BaseOptions {
        val imported = PoseModelStore.get(context)

        if (imported != null && imported.file.length() > 1024L) {
            try {
                val bytes = imported.file.readBytes()

                // MediaPipe Tasks expects a direct ByteBuffer for modelAssetBuffer.
                val buffer = ByteBuffer
                    .allocateDirect(bytes.size)
                    .order(ByteOrder.nativeOrder())
                buffer.put(bytes)
                buffer.rewind()

                return BaseOptions.builder()
                    .setModelAssetBuffer(buffer)
                    .build()
            } catch (_: Exception) {
                // A bad/incompatible imported model must not prevent the
                // bundled official model from being used.
                PoseModelStore.clear(context)
            }
        }

        return BaseOptions.builder()
            .setModelAssetPath(MODEL_ASSET)
            .build()
    }

    private val landmarker: PoseLandmarker

    init {
        val base = createBaseOptions(context)
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(base)
            .setRunningMode(RunningMode.IMAGE)
            .setNumPoses(1)
            .build()

        landmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detect(bitmap: Bitmap): PoseLandmarkerResult =
        landmarker.detect(BitmapImageBuilder(bitmap).build())

    fun close() = landmarker.close()
}
