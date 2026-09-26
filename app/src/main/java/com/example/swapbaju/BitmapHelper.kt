package com.example.swapbaju

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.max

object BitmapHelper {

    private const val MAX_SIDE = 1536

    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                decodeOriented(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun resizeToSafeSize(bitmap: Bitmap): Bitmap {
        val maxSide = max(bitmap.width, bitmap.height)
        if (maxSide <= MAX_SIDE) return bitmap

        val scale = MAX_SIDE.toFloat() / maxSide.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun decodeOriented(stream: InputStream): Bitmap? {
        val bytes = stream.readBytes()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return null

        val exif = ExifInterface(bytes.inputStream())
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 ->
                matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 ->
                matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 ->
                matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
        }

        if (matrix.isIdentity) return bitmap

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        ).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }
}
