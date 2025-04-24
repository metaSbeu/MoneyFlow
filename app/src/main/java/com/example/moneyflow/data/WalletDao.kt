package com.example.moneyflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT COUNT(*) FROM wallets")
    fun getCount(): Single<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(wallet: List<Wallet>): Completable

    @Query("SELECT * FROM wallets WHERE id = :walletId LIMIT 1")
    fun getWalletById(walletId: Int): Single<Wallet>

    @androidx.room.Update
    fun update(wallet: Wallet): Completable
}