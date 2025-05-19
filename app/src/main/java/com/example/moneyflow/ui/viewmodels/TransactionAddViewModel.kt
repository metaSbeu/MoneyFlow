package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.Wallet
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class TransactionAddViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _shouldCloseScreen = MutableLiveData<Boolean>()
    val shouldCloseScreen: LiveData<Boolean> get() = _shouldCloseScreen

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet> get() = _wallet

    fun refreshCategories() {
        val disposable = database.categoryDao().getCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _categories.value = it
                    Log.d(TAG, "refreshCategories: success")
                },
                {
                    Log.d(TAG, it.toString())
                }
            )
        compositeDisposable.add(disposable)
    }

    fun getWalletById(walletId: Int) {
        val disposable = database.walletDao().getWalletById(walletId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _wallet.value = it
            }
        compositeDisposable.add(disposable)
    }

    fun updateWalletBalance(wallet: Wallet) {
        val disposable = database.walletDao().update(wallet)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

            }
        compositeDisposable.add(disposable)
    }

    fun saveTransaction(transaction: Transaction) {
        val walletDisposable = database.walletDao().getWalletById(transaction.walletId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ wallet ->
                val newBalance = if (transaction.isIncome) {
                    wallet.balance + transaction.sum
                } else {
                    wallet.balance - transaction.sum
                }

                val updatedWallet = wallet.copy(balance = newBalance)
                updateWalletBalance(updatedWallet)

                val transactionDisposable = database.transactionDao().insert(transaction)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _shouldCloseScreen.value = true
                        Log.d(TAG, "saveTransaction: success")
                    }, {
                        Log.d(TAG, it.toString())
                    })
                compositeDisposable.add(transactionDisposable)
            }, {
                Log.d(TAG, "saveTransaction: cannot find wallet: ${it.message}")
            })

        compositeDisposable.add(walletDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    companion object {
        const val TAG = "TransactionAddViewModel"
    }
}