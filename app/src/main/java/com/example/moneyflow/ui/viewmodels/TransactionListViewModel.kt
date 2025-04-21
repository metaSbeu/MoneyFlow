package com.example.moneyflow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.TransactionWithCategory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)

    val transactions: LiveData<List<TransactionWithCategory>> get() = database.transactionDao().getAllWithCategory()

    override fun onCleared() {
        super.onCleared()
    }
}