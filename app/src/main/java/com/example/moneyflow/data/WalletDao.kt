package com.example.moneyflow.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets")
    fun getWallets(): Single<List<Wallet>>

    @Insert(onConflict = REPLACE)
    fun insert(wallet: Wallet): Completable

    @Query("DELETE FROM wallets WHERE id = :id")
    fun delete(id: Int): Completable
}