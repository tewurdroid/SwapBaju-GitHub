package com.example.swapbaju

import android.graphics.Bitmap

class GeometricVtonEngine(
    private val renderer: VtonRenderer = AlphaVtonRenderer()
) : VtonEngine {

    override fun run(
        person: Bitmap,
        garment: Bitmap,
        garmentMask: Bitmap?,
        bodyMask: Bitmap?
    ): Bitmap {
        return renderer.render(
            person = person,
            warpedGarment = garment,
            garmentMask = garmentMask,
            bodyMask = bodyMask
        )
    }
}
