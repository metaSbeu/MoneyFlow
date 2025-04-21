package com.example.moneyflow.data

import androidx.room.*

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("walletId")]
)
data class Transaction(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val categoryId: Int,
    val walletId: Int,

    val sum: Double,
    val isIncome: Boolean,

    val note: String?,
    val createdAt: Long = System.currentTimeMillis()
)
