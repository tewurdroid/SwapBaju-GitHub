package com.example.swapbaju

import android.graphics.Bitmap

/**
 * Abstraction for the actual virtual try-on inference stage.
 *
 * The current implementation is an offline geometric fallback.
 * A TFLite/ONNX VTON implementation can replace it later without
 * changing the Activity/UI layer.
 */
interface VtonEngine {
    fun run(
        person: Bitmap,
        garment: Bitmap,
        garmentMask: Bitmap?,
        bodyMask: Bitmap?
    ): Bitmap
}
