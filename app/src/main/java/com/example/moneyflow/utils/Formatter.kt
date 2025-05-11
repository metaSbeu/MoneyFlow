package com.example.moneyflow.utils

import android.content.Context
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatter {
    fun Double.formatWithSpaces(context: Context): String {
        val selectedCurrency = PreferenceManager.getSelectedCurrency(context)
        val usdRate = PreferenceManager.getSavedUsdRate(context)
        val eurRate = PreferenceManager.getSavedEurRate(context)

        val convertedValue = when (selectedCurrency) {
            "USD" -> this * usdRate
            "EUR" -> this * eurRate
            else -> this // RUB или по умолчанию
        }

        val formatter = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.getDefault())).apply {
            groupingSize = 3
            isDecimalSeparatorAlwaysShown = false
        }

        val formatted = formatter.format(convertedValue)
        val currencySymbol = when (selectedCurrency) {
            "USD" -> "$"
            "EUR" -> "€"
            else -> "₽"
        }

        return "$formatted $currencySymbol"
    }

    fun convertToRub(context: Context, amount: Double): Double {
        return when (PreferenceManager.getSelectedCurrency(context)) {
            "USD" -> amount / PreferenceManager.getSavedUsdRate(context)
            "EUR" -> amount / PreferenceManager.getSavedEurRate(context)
            else -> amount // RUB
        }
    }
}
