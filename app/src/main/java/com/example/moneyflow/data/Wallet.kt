package com.example.moneyflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Int,
    val icon: String,

    val currency: String = "RUB",
    val type: String
)
