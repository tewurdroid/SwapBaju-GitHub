package com.example.swapbaju.model

import android.content.Context
import org.json.JSONObject

object HumanParsingConfigLoader {

    fun load(context: Context, assetName: String): HumanParsingModelConfig? {
        return runCatching {
            val text = context.assets.open(assetName).bufferedReader().use { it.readText() }
            val json = JSONObject(text)

            val upper = json.optJSONArray("upperGarmentClasses")
                ?.let { array ->
                    buildSet {
                        for (i in 0 until array.length()) add(array.getInt(i))
                    }
                } ?: emptySet()

            val lower = json.optJSONArray("lowerGarmentClasses")
                ?.let { array ->
                    buildSet {
                        for (i in 0 until array.length()) add(array.getInt(i))
                    }
                } ?: emptySet()

            val skin = json.optJSONArray("skinClasses")
                ?.let { array ->
                    buildSet {
                        for (i in 0 until array.length()) add(array.getInt(i))
                    }
                } ?: emptySet()

            val hair = json.optJSONArray("hairClasses")
                ?.let { array ->
                    buildSet {
                        for (i in 0 until array.length()) add(array.getInt(i))
                    }
                } ?: emptySet()

            HumanParsingModelConfig(
                assetName = json.getString("assetName"),
                inputWidth = json.getInt("inputWidth"),
                inputHeight = json.getInt("inputHeight"),
                mean = json.optJSONArray("mean").toFloatArray(3, 0f),
                std = json.optJSONArray("std").toFloatArray(3, 1f),
                outputClasses = json.getInt("outputClasses"),
                upperGarmentClasses = upper,
                lowerGarmentClasses = lower,
                skinClasses = skin,
                hairClasses = hair,
                backgroundClass = json.optInt("backgroundClass", 0),
                confidenceThreshold = json.optDouble("confidenceThreshold", 0.5).toFloat()
            )
        }.getOrNull()
    }

    private fun org.json.JSONArray.toFloatArray(size: Int, fallback: Float): FloatArray {
        val out = FloatArray(size) { fallback }
        for (i in 0 until minOf(length(), size)) {
            out[i] = optDouble(i, fallback).toFloat()
        }
        return out
    }
}
