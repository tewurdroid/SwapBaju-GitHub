package com.example.swapbaju

import android.graphics.Bitmap

/**
 * Safe fallback: returns the composited result unchanged.
 */
class IdentityImageRefiner : ImageRefiner {
    override fun refine(input: RefinementInput): Bitmap {
        return input.composite.copy(Bitmap.Config.ARGB_8888, true)
    }
}
