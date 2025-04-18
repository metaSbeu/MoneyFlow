package com.example.moneyflow

data class Transaction(
    val category: String,
    val amount: Int,
    val isIncome: Boolean
)
