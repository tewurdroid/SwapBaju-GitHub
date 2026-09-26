package com.example.swapbaju

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.BlurMaskFilter
import kotlin.math.max

object MaskUtils {

    fun resizeMask(mask: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createScaledBitmap(mask, width, height, true)

    fun multiply(a: Bitmap, b: Bitmap): Bitmap {
        require(a.width == b.width && a.height == b.height)

        val out = Bitmap.createBitmap(a.width, a.height, Bitmap.Config.ALPHA_8)
        val ap = IntArray(a.width * a.height)
        val bp = IntArray(b.width * b.height)

        a.getPixels(ap, 0, a.width, 0, 0, a.width, a.height)
        b.getPixels(bp, 0, b.width, 0, 0, b.width, b.height)

        for (i in ap.indices) {
            ap[i] = (ap[i] * bp[i] / 255).coerceIn(0, 255)
        }

        out.setPixels(ap, 0, a.width, 0, 0, a.width, a.height)
        return out
    }

    fun feather(mask: Bitmap, radius: Float): Bitmap {
        val out = Bitmap.createBitmap(
            mask.width,
            mask.height,
            Bitmap.Config.ALPHA_8
        )
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            maskFilter = BlurMaskFilter(
                max(0.5f, radius),
                BlurMaskFilter.Blur.NORMAL
            )
        }
        canvas.drawBitmap(mask, 0f, 0f, paint)
        return out
    }

    fun applyAlpha(source: Bitmap, mask: Bitmap): Bitmap {
        require(source.width == mask.width && source.height == mask.height)

        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null
        return out
    }
}
