package com.example.moneyflow.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatter {
    fun Double.formatWithSpaces(): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault())).apply {
            groupingSize = 3
            isDecimalSeparatorAlwaysShown = false
        }
        return formatter.format(this)
    }
}