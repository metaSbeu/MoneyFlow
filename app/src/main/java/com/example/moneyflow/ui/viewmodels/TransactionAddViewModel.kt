package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.TransactionWithCategory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class TransactionAddViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _shouldCloseScreen = MutableLiveData<Boolean>()
    val shouldCloseScreen: LiveData<Boolean> get() = _shouldCloseScreen

    val categories: LiveData<List<Category>> get() = database.categoryDao().getCategories()

    fun saveTransaction(transaction: Transaction) {
        val disposable = database.transactionDao().insert(transaction)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _shouldCloseScreen.value = true
                    Log.d(TAG, "saveTransaction: success")
                },
                {
                    Log.d(TAG, it.toString())
                }
            )
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        const val TAG = "TransactionAddViewModel"
    }
}