package com.example.moneyflow.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "categories")
data class Category (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,

    @DrawableRes val iconResId: Int,

    val isIncome: Boolean
): Serializable
