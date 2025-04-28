package com.example.moneyflow.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceManager {

    private const val PREFERENCE_NAME = "app_prefs"
    private const val KEY_DEFAULT_CATEGORIES_ADD = "default_categories_added"
    private const val IS_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_USER_PIN = "user_pin"

    fun areDefaultCategoriesAdded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DEFAULT_CATEGORIES_ADD, false)
    }

    fun setDefaultCategories(context: Context) {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_DEFAULT_CATEGORIES_ADD, true) }
    }

    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(context: Context, isFirstLaunch: Boolean) {
        getPrefs(context).edit { putBoolean(IS_FIRST_LAUNCH, isFirstLaunch) }
    }

    fun getPinCode(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_PIN, null)
    }

    fun setPinCode(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_USER_PIN, pin) }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
}