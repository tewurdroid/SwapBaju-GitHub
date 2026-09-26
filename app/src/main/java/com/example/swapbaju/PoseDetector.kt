package com.example.swapbaju

import com.example.swapbaju.model.PoseModelStore

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseDetector(context: Context) {

    private fun createBaseOptions(context: Context): BaseOptions {
        val imported = PoseModelStore.get(context)
        return if (imported != null) {
            BaseOptions.builder()
                .setModelAssetBuffer(
                    java.nio.ByteBuffer.wrap(imported.file.readBytes())
                )
                .build()
        } else {
            BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .build()
        }
    }


    private val landmarker: PoseLandmarker
    init {
        val base = BaseOptions.builder().setModelAssetPath("pose_landmarker_full.task").build()
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
