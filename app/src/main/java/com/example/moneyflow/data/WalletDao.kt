package com.example.moneyflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets")
    fun getWallets(): List<Wallet>

    @Insert(onConflict = REPLACE)
    fun insert(wallet: Wallet)

    @Query("DELETE FROM wallets WHERE id = :id")
    fun delete(id: Int)
}