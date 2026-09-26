package com.example.swapbaju

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore

object ImageSaver {
    fun save(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "SwapBaju_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SwapBaju")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: return false

            context.contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            } ?: return false

            true
        } catch (_: Exception) {
            false
        }
    }
}
