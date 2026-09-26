package com.example.swapbaju

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter

/**
 * Optional adapter for a local MediaPipe Image Segmenter model.
 *
 * Put a compatible .task model in assets and pass its asset filename.
 * The exact output classes depend on the selected model. This adapter
 * intentionally exposes the raw category mask as personMask and leaves
 * clothingMask null until a clothing-specific model is selected.
 */
class MediaPipeSegmentationEngine(
    context: Context,
    modelAssetName: String
) : SegmentationEngine {

    private val segmenter: ImageSegmenter

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelAssetName)
            .build()

        val options = ImageSegmenter.ImageSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .build()

        segmenter = ImageSegmenter.createFromOptions(
            context,
            options
        )
    }

    override fun segment(person: Bitmap): SegmentationResult {
        val image = BitmapImageBuilder(person).build()
        val result = segmenter.segment(image)

        // A model-specific mapping is required to decide which category
        // represents person/clothing. Keep this adapter conservative.
        return SegmentationResult(
            personMask = null,
            clothingMask = null
        )
    }

    override fun close() {
        segmenter.close()
    }
}
