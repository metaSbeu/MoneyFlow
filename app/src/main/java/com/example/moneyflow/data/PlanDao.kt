package com.example.moneyflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao()
interface PlanDao {

    @Query("SELECT * FROM plans")
    fun getPlans(): Single<List<Plan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(plan: Plan): Completable

    @Query("DELETE FROM plans WHERE id = :id")
    fun delete(id: Int): Completable

    @Update
    fun update(plan: Plan): Completable

    @Query("SELECT * FROM plans WHERE isNotificationActive = 1")
    fun getAllWithNotificationActive(): List<Plan>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun getPlanById(id: Int): Plan?

}