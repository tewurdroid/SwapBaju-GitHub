package com.example.swapbaju.model

import android.content.Context
import org.json.JSONObject

data class RefinementRuntimeConfig(
    val inputWidth: Int,
    val inputHeight: Int,
    val inputMin: Float,
    val inputMax: Float,
    val outputMin: Float,
    val outputMax: Float,
    val outputChannels: Int = 3
)

object RefinementRuntimeConfigStore {
    private const val PREFS = "refinement_runtime_config"
    private const val KEY = "config"

    fun save(context: Context, c: RefinementRuntimeConfig) {
        val j = JSONObject()
            .put("inputWidth", c.inputWidth)
            .put("inputHeight", c.inputHeight)
            .put("inputMin", c.inputMin)
            .put("inputMax", c.inputMax)
            .put("outputMin", c.outputMin)
            .put("outputMax", c.outputMax)
            .put("outputChannels", c.outputChannels)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, j.toString()).apply()
    }

    fun load(context: Context): RefinementRuntimeConfig? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return null
        return runCatching {
            val j = JSONObject(raw)
            RefinementRuntimeConfig(
                j.getInt("inputWidth"),
                j.getInt("inputHeight"),
                j.optDouble("inputMin", 0.0).toFloat(),
                j.optDouble("inputMax", 1.0).toFloat(),
                j.optDouble("outputMin", 0.0).toFloat(),
                j.optDouble("outputMax", 1.0).toFloat(),
                j.optInt("outputChannels", 3)
            )
        }.getOrNull()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY).apply()
    }
}
