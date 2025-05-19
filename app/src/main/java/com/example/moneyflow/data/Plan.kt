package com.example.moneyflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "plans")
data class Plan(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sum: Double,
    val targetNotificationDayOfMonth: Int,
    val isNotificationActive: Boolean = false
) : Serializable