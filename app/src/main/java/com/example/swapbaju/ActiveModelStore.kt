package com.example.swapbaju

import android.content.Context

object ActiveModelStore {

    private const val PREFS = "swapbaju_active_models"

    fun setActive(context: Context, type: ModelType, id: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(type.name, id)
            .apply()
    }

    fun getActiveId(context: Context, type: ModelType): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(type.name, null)

    fun clear(context: Context, type: ModelType) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(type.name)
            .apply()
    }
}
