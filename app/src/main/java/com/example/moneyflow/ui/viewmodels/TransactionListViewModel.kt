package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.TransactionWithCategory
import com.example.moneyflow.data.Wallet
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Date

class TransactionListViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MainDatabase.getDb(application)

    private val _transactions = MutableLiveData<List<TransactionWithCategory>>()
    val transactions: LiveData<List<TransactionWithCategory>> get() = _transactions

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet> get() = _wallet

    private val compositeDisposable = CompositeDisposable()

    private var allTransactions: List<TransactionWithCategory> = emptyList()

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> get() = _categories

    private var currentFilterType: FilterType = FilterType.ALL
    private var currentCategoryFilter: String? = null
    private var currentDateFilterStart: Date? = null
    private var currentDateFilterEnd: Date? = null

    fun refreshWallet(wallet: Wallet) {
        val disposable = database.walletDao().getWalletById(wallet.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _wallet.value = it
            })
        compositeDisposable.add(disposable)
    }

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
                    val uniqueCategories = transactions.mapNotNull { it.category.name }.distinct()
                    _categories.value = uniqueCategories
                    reapplyFilters() // Применяем фильтры после загрузки данных
                    Log.d(TAG, "loadTransactions: success, count = ${transactions.size}")
                },
                { error ->
                    Log.e(TAG, "loadTransactions error: ${error.message}")
                }
            )
        compositeDisposable.add(disposable)
    }

    fun filterTransactionsByCategory(category: String) {
        currentCategoryFilter = if (category == "Все категории") null else category
        reapplyFilters()
    }

    fun sortTransactionsByDate(isAscending: Boolean, startDate: Date, endDate: Date) {
        currentDateFilterStart = startDate
        currentDateFilterEnd = endDate
        reapplyFilters()
    }

    fun filterTransactionsByType(filterType: FilterType) {
        currentFilterType = filterType
        reapplyFilters()
    }

    private fun reapplyFilters() {
        var filteredTransactions = allTransactions

        // Фильтрация по типу (доход/расход)
        filteredTransactions = when (currentFilterType) {
            FilterType.ALL -> filteredTransactions
            FilterType.INCOME -> filteredTransactions.filter { it.transaction.isIncome }
            FilterType.EXPENSE -> filteredTransactions.filter { !it.transaction.isIncome }
        }

        // Фильтрация по категории
        currentCategoryFilter?.let { category ->
            filteredTransactions = filteredTransactions.filter { it.category.name == category }
        }

        // Фильтрация по дате
        if (currentDateFilterStart != null && currentDateFilterEnd != null) {
            filteredTransactions = filteredTransactions.filter {
                it.transaction.createdAt in currentDateFilterStart!!.time..currentDateFilterEnd!!.time
            }
        }

        _transactions.value = filteredTransactions.sortedByDescending { it.transaction.createdAt }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    enum class FilterType {
        ALL, INCOME, EXPENSE
    }

    companion object {
        const val TAG = "TransactionListViewModel"
    }
}