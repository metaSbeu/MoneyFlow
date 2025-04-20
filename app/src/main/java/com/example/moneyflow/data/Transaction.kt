package com.example.moneyflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val amount: Int,
    val isIncome: Boolean
)
