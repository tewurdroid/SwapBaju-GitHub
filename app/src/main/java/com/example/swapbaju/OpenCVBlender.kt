package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.BlurMaskFilter

object OpenCVBlender {

    fun blend(
        personBitmap: Bitmap,
        warpedCloth: Bitmap,
        bodyMask: Bitmap?
    ): Bitmap {

        val result = personBitmap.copy(
            Bitmap.Config.ARGB_8888,
            true
        )

        val canvas = Canvas(result)
        val paint = Paint(
            Paint.ANTI_ALIAS_FLAG or
                Paint.FILTER_BITMAP_FLAG
        )

        if (bodyMask == null) {
            canvas.drawBitmap(warpedCloth, 0f, 0f, paint)
            return result
        }

        val layer = Bitmap.createBitmap(
            result.width,
            result.height,
            Bitmap.Config.ARGB_8888
        )

        val layerCanvas = Canvas(layer)
        layerCanvas.drawBitmap(
            warpedCloth,
            0f,
            0f,
            paint
        )

        // Feathering prevents a hard polygon edge.
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskPaint.maskFilter = BlurMaskFilter(
            2.5f,
            BlurMaskFilter.Blur.NORMAL
        )
        maskPaint.xfermode = PorterDuffXfermode(
            PorterDuff.Mode.DST_IN
        )

        layerCanvas.drawBitmap(
            bodyMask,
            0f,
            0f,
            maskPaint
        )

        maskPaint.xfermode = null
        maskPaint.maskFilter = null

        canvas.drawBitmap(
            layer,
            0f,
            0f,
            paint
        )

        return result
    }
}
