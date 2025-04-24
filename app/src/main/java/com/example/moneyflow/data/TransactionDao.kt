package com.example.moneyflow.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions")
    fun getTransaction(): Single<List<Transaction>>

    @Insert(onConflict = REPLACE)
    fun insert(transaction: Transaction): Completable

    @Query("DELETE FROM transactions WHERE id = :id")
    fun remove(id: Int): Completable

    @Query("SELECT * FROM transactions")
    fun getAllWithCategory(): Single<List<TransactionWithCategory>>

    @androidx.room.Update
    fun update(transaction: Transaction): Completable

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    fun getTransactionById(transactionId: Int): Single<Transaction>
}