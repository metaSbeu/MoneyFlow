package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.TransactionWithCategory
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel.Companion.TAG
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)

    private val _transactions = MutableLiveData<List<TransactionWithCategory>>()
    val transactions: LiveData<List<TransactionWithCategory>> get() = _transactions

    private val compositeDisposable = CompositeDisposable()

    private var allTransactions: List<TransactionWithCategory> = emptyList()

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    fun loadTransactions(walletId: Int? = null) {
        val disposable = if (walletId == null) {
            database.transactionDao().getAllWithCategory()
        } else {
            database.transactionDao().getTransactionsByWalletIdWithCategory(walletId)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { transactions ->
                    allTransactions = transactions
                    _transactions.value = transactions

                    // Заполняем категории
                    val uniqueCategories = transactions.mapNotNull { it.category.name }.distinct()
                    _categories.value = uniqueCategories

                    Log.d(TAG, "loadTransactions: success, count = ${transactions.size}")
                },
                { error ->
                    Log.e(TAG, "loadTransactions error: ${error.message}")
                }
            )
        compositeDisposable.add(disposable)
    }

    fun filterTransactionsByCategory(category: String) {
        if (category == "Все категории") {
            _transactions.value = allTransactions
        } else {
            _transactions.value = allTransactions.filter { it.category.name == category }
        }
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
