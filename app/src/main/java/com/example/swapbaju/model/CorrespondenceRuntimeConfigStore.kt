package com.example.swapbaju.model


import com.example.swapbaju.*
import android.content.Context
import org.json.JSONObject

data class CorrespondenceRuntimeConfig(
    val inputWidth: Int,
    val inputHeight: Int,
    val outputWidth: Int,
    val outputHeight: Int,
    val inputRangeMin: Float,
    val inputRangeMax: Float,
    val outputXMin: Float,
    val outputXMax: Float,
    val outputYMin: Float,
    val outputYMax: Float,
    val confidenceMin: Float,
    val confidenceMax: Float
)

object CorrespondenceRuntimeConfigStore {
    private const val PREFS = "correspondence_runtime_config"
    private const val KEY = "config"

    fun save(context: Context, c: CorrespondenceRuntimeConfig) {
        val j = JSONObject()
            .put("inputWidth", c.inputWidth)
            .put("inputHeight", c.inputHeight)
            .put("outputWidth", c.outputWidth)
            .put("outputHeight", c.outputHeight)
            .put("inputRangeMin", c.inputRangeMin)
            .put("inputRangeMax", c.inputRangeMax)
            .put("outputXMin", c.outputXMin)
            .put("outputXMax", c.outputXMax)
            .put("outputYMin", c.outputYMin)
            .put("outputYMax", c.outputYMax)
            .put("confidenceMin", c.confidenceMin)
            .put("confidenceMax", c.confidenceMax)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, j.toString()).apply()
    }

    fun load(context: Context): CorrespondenceRuntimeConfig? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return null
        return runCatching {
            val j = JSONObject(raw)
            CorrespondenceRuntimeConfig(
                j.getInt("inputWidth"), j.getInt("inputHeight"),
                j.getInt("outputWidth"), j.getInt("outputHeight"),
                j.optDouble("inputRangeMin", 0.0).toFloat(),
                j.optDouble("inputRangeMax", 1.0).toFloat(),
                j.optDouble("outputXMin", 0.0).toFloat(),
                j.optDouble("outputXMax", 1.0).toFloat(),
                j.optDouble("outputYMin", 0.0).toFloat(),
                j.optDouble("outputYMax", 1.0).toFloat(),
                j.optDouble("confidenceMin", 0.0).toFloat(),
                j.optDouble("confidenceMax", 1.0).toFloat()
            )
        }.getOrNull()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY).apply()
    }
}
