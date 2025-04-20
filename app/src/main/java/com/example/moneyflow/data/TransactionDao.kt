package com.example.moneyflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions")
    fun getTransaction(): List<Transaction>

    @Insert(onConflict = REPLACE)
    fun insert(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    fun remove(id: Int)
}