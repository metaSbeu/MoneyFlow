package com.example.moneyflow.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceManager {

    private const val PREFERENCE_NAME = "app_prefs"
    private const val KEY_DEFAULT_CATEGORIES_ADD = "default_categories_added"
    private const val IS_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_USER_PIN = "user_pin"
    private const val KEY_RATE_USD = "rate_usd"
    private const val KEY_RATE_EUR = "rate_eur"
    private const val KEY_SELECTED_CURRENCY = "selected_currency"
    private const val KEY_SELECTED_THEME = "selected_theme"
    private const val KEY_NOTIFICATION_TYPE1 = "notification_type1"
    private const val KEY_NOTIFICATION_TYPE2 = "notification_type2"

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

    fun saveCurrencyRates(context: Context, usd: Double?, eur: Double?) {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putFloat(KEY_RATE_USD, usd?.toFloat() ?: 0f)
            putFloat(KEY_RATE_EUR, eur?.toFloat() ?: 0f)
        }
    }

    fun getSavedUsdRate(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_RATE_USD, 0f).toDouble()
    }

    fun getSavedEurRate(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_RATE_EUR, 0f).toDouble()
    }

    fun setSelectedCurrency(context: Context, currencyCode: String) {
        getPrefs(context).edit {
            putString(KEY_SELECTED_CURRENCY, currencyCode)
        }
    }

    fun getSelectedCurrency(context: Context): String {
        return getPrefs(context).getString(KEY_SELECTED_CURRENCY, "RUB") ?: "RUB"
    }

    fun setSelectedTheme(context: Context, theme: String) {
        getPrefs(context).edit {
            putString(KEY_SELECTED_THEME, theme)
        }
    }

    fun getSelectedTheme(context: Context): String {
        return getPrefs(context).getString(KEY_SELECTED_THEME, "System") ?: "System"
    }

    fun isNotificationType1Enabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATION_TYPE1, true)
    }

    fun setNotificationType1Enabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_NOTIFICATION_TYPE1, enabled) }
    }

    fun isNotificationType2Enabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATION_TYPE2, true)
    }

    fun setNotificationType2Enabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_NOTIFICATION_TYPE2, enabled) }
    }
}