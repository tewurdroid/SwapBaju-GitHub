package com.example.swapbaju.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HumanParsingRuntimeConfig(
    val inputWidth: Int,
    val inputHeight: Int,
    val outputClasses: Int,
    val backgroundClass: Int,
    val upperGarmentClasses: Set<Int>,
    val lowerGarmentClasses: Set<Int>,
    val mean: FloatArray,
    val std: FloatArray,
    val confidenceThreshold: Float
) {
    fun toModelConfig(assetName: String): HumanParsingModelConfig =
        HumanParsingModelConfig(
            assetName = assetName,
            inputWidth = inputWidth,
            inputHeight = inputHeight,
            mean = mean,
            std = std,
            outputClasses = outputClasses,
            upperGarmentClasses = upperGarmentClasses,
            lowerGarmentClasses = lowerGarmentClasses,
            backgroundClass = backgroundClass,
            confidenceThreshold = confidenceThreshold
        )
}

object HumanParsingRuntimeConfigStore {
    private const val PREFS = "human_parsing_runtime_config"
    private const val KEY = "config"

    fun save(context: Context, config: HumanParsingRuntimeConfig) {
        val json = JSONObject()
            .put("inputWidth", config.inputWidth)
            .put("inputHeight", config.inputHeight)
            .put("outputClasses", config.outputClasses)
            .put("backgroundClass", config.backgroundClass)
            .put("upperGarmentClasses", JSONArray(config.upperGarmentClasses.toList()))
            .put("lowerGarmentClasses", JSONArray(config.lowerGarmentClasses.toList()))
            .put("mean", JSONArray(config.mean.toList()))
            .put("std", JSONArray(config.std.toList()))
            .put("confidenceThreshold", config.confidenceThreshold.toDouble())

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, json.toString())
            .apply()
    }

    fun load(context: Context): HumanParsingRuntimeConfig? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return null

        return runCatching {
            val j = JSONObject(raw)
            HumanParsingRuntimeConfig(
                inputWidth = j.getInt("inputWidth"),
                inputHeight = j.getInt("inputHeight"),
                outputClasses = j.getInt("outputClasses"),
                backgroundClass = j.optInt("backgroundClass", 0),
                upperGarmentClasses = j.getJSONArray("upperGarmentClasses").toIntSet(),
                lowerGarmentClasses = j.getJSONArray("lowerGarmentClasses").toIntSet(),
                mean = j.getJSONArray("mean").toFloatArray(3),
                std = j.getJSONArray("std").toFloatArray(3),
                confidenceThreshold = j.optDouble("confidenceThreshold", 0.5).toFloat()
            )
        }.getOrNull()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY)
            .apply()
    }

    private fun JSONArray.toIntSet(): Set<Int> =
        buildSet {
            for (i in 0 until length()) add(getInt(i))
        }

    private fun JSONArray.toFloatArray(size: Int): FloatArray =
        FloatArray(size) { i -> optDouble(i, 0.0).toFloat() }
}
