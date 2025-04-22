package com.example.moneyflow.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val balance: Int,

    @DrawableRes val iconResId: Int,

    val currency: String = "RUB",
    val type: String
)
