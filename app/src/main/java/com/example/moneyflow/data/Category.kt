package com.example.moneyflow.data

import android.graphics.drawable.Drawable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val icon: String
)
