package com.example.moneyflow.data

import android.content.Context
import androidx.core.content.edit

object PreferenceManager {

    private const val PREFERENCE_NAME = "app_preferences"
    private const val KEY_DEFAULT_CATEGORIES_ADD = "default_categories_added"

    fun areDefaultCategoriesAdded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DEFAULT_CATEGORIES_ADD, false)
    }

    fun setDefaultCategories(context: Context) {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_DEFAULT_CATEGORIES_ADD, true) }
    }
}