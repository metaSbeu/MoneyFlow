package com.example.moneyflow.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    fun getCategories(): Single<List<Category>>

    @Insert(onConflict = REPLACE)
    fun insert(category: Category): Completable

    @Query("DELETE FROM categories WHERE id = :id")
    fun delete(id: Int): Completable

    @Query("SELECT COUNT(*) FROM categories")
    fun getCount(): Single<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(categories: List<Category>): Completable
}