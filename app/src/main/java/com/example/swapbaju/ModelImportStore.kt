package com.example.swapbaju

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ModelImportStore {

    private const val PREFS = "swapbaju_models"
    private const val KEY_MODELS = "models"

    fun load(context: Context): List<ImportedModel> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODELS, "[]") ?: "[]"

        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(
                    ImportedModel(
                        id = o.getString("id"),
                        displayName = o.getString("displayName"),
                        type = ModelType.valueOf(o.getString("type")),
                        uri = o.getString("uri"),
                        sizeBytes = o.getLong("sizeBytes"),
                        inputShapes = readShapes(o.optJSONArray("inputShapes")),
                        outputShapes = readShapes(o.optJSONArray("outputShapes")),
                        valid = o.getBoolean("valid"),
                        error = o.optString("error").takeIf { it.isNotEmpty() }
                    )
                )
            }
        }
    }

    fun save(context: Context, model: ImportedModel) {
        val models = load(context)
            .filterNot { it.id == model.id }
            .toMutableList()

        models += model

        val array = JSONArray()
        models.forEach { array.put(toJson(it)) }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODELS, array.toString())
            .apply()
    }

    fun remove(context: Context, id: String) {
        val array = JSONArray()
        load(context)
            .filterNot { it.id == id }
            .forEach { array.put(toJson(it)) }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODELS, array.toString())
            .apply()
    }

    private fun toJson(model: ImportedModel): JSONObject =
        JSONObject().apply {
            put("id", model.id)
            put("displayName", model.displayName)
            put("type", model.type.name)
            put("uri", model.uri)
            put("sizeBytes", model.sizeBytes)
            put("inputShapes", writeShapes(model.inputShapes))
            put("outputShapes", writeShapes(model.outputShapes))
            put("valid", model.valid)
            put("error", model.error ?: "")
        }

    private fun writeShapes(shapes: List<List<Int>>): JSONArray =
        JSONArray().apply {
            shapes.forEach { shape ->
                put(JSONArray().apply {
                    shape.forEach { put(it) }
                })
            }
        }

    private fun readShapes(array: JSONArray?): List<List<Int>> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val row = array.getJSONArray(i)
                add(buildList {
                    for (j in 0 until row.length()) add(row.getInt(j))
                })
            }
        }
    }
}
