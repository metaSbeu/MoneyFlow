package com.example.moneyflow.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, Transaction::class, Wallet::class],
    version = 2
)
abstract class MainDatabase : RoomDatabase() {

    companion object {

        @Volatile
        private var INSTANCE: MainDatabase? = null
        fun getDb(application: Application): MainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    MainDatabase::class.java,
                    "database.db"
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    abstract fun categoryDao(): CategoryDao
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
}