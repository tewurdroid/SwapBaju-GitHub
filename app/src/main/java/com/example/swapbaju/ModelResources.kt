package com.example.swapbaju

import android.content.Context

/**
 * Central place for validating model assets before runtime inference.
 */
object ModelResources {

    fun exists(context: Context, asset: String): Boolean {
        return try {
            context.assets.open(asset).use { true }
        } catch (_: Exception) {
            false
        }
    }

    fun require(context: Context, asset: String) {
        check(exists(context, asset)) {
            "Missing model asset: $asset"
        }
    }
}
