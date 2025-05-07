package com.example.moneyflow.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatter {
    fun Double.formatWithSpaces(currencySymbol: String? = null): String {
        val formatter = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.getDefault())).apply {
            groupingSize = 3
            isDecimalSeparatorAlwaysShown = false
        }
        val formatted = formatter.format(this)
        return if (currencySymbol != null) "$formatted $currencySymbol" else formatted
    }
}
