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
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel.Companion.TAG
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class TransactionEditViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> get() = _transaction

    private val _shouldCloseScreen = MutableLiveData<Boolean>()
    val shouldCloseScreen: LiveData<Boolean> get() = _shouldCloseScreen

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    private val _selectedWalletId = MutableLiveData<Int?>()

    fun setSelectedWalletId(walletId: Int) {
        _selectedWalletId.value = walletId
    }

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

    fun getTransactionById(transactionId: Int) {
        val disposable = database.transactionDao().getTransactionById(transactionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _transaction.value = it
                },
                {
                    Log.d("TransactionEditViewModel", "getTransactionById: $it")
                }
            )
        compositeDisposable.add(disposable)
    }

    fun getCategoryById(categoryId: Int, callback: (Category?) -> Unit) {
        val disposable = database.categoryDao().getCategoryById(categoryId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    callback(it)
                },
                {
                    Log.e(TAG, "Error getting category", it)
                    callback(null)
                }
            )
        compositeDisposable.add(disposable)
    }

    fun getWalletById(walletId: Int, callback: (Wallet?) -> Unit) {
        val disposable = database.walletDao().getWalletById(walletId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    callback(it)
                },
                {
                    Log.e(TAG, "Error getting wallet", it)
                    callback(null)
                }
            )
        compositeDisposable.add(disposable)
    }

    fun getAllWallets(callback: (List<Wallet>) -> Unit) {
        val disposable = database.walletDao().getWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    callback(it)
                },
                {
                    Log.e(TAG, "Error getting wallets", it)
                    callback(emptyList())
                }
            )
        compositeDisposable.add(disposable)
    }

    fun deleteTransaction(transactionId: Int) {
        val getTransactionDisposable = database.transactionDao().getTransactionById(transactionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ transaction ->
                val getWalletDisposable = database.walletDao().getWalletById(transaction.walletId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ wallet ->
                        val newBalance = if (transaction.isIncome) {
                            wallet.balance - transaction.sum
                        } else {
                            wallet.balance + transaction.sum
                        }

                        val updateWalletDisposable =
                            database.walletDao().update(wallet.copy(balance = newBalance))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    val deleteTransactionDisposable =
                                        database.transactionDao().remove(transactionId)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                _shouldCloseScreen.value = true
                                            }, { error ->
                                                Log.e(
                                                    "TransactionEditViewModel",
                                                    "Error deleting transaction",
                                                    error
                                                )
                                            })
                                    compositeDisposable.add(deleteTransactionDisposable)
                                }, { error ->
                                    Log.e(
                                        "TransactionEditViewModel",
                                        "Error updating wallet balance",
                                        error
                                    )
                                })
                        compositeDisposable.add(updateWalletDisposable)
                    }, { error ->
                        Log.e("TransactionEditViewModel", "Error getting wallet", error)
                    })
                compositeDisposable.add(getWalletDisposable)
            }, { error ->
                Log.e("TransactionEditViewModel", "Error getting transaction", error)
            })

        compositeDisposable.add(getTransactionDisposable)
    }

    fun updateTransaction(transaction: Transaction) {
        val currentTransaction = _transaction.value ?: return
        val newWalletId = _selectedWalletId.value ?: currentTransaction.walletId

        val getOldWalletDisposable = database.walletDao().getWalletById(currentTransaction.walletId)
            .subscribeOn(Schedulers.io())
            .flatMap { oldWallet ->
                database.walletDao().getWalletById(newWalletId)
                    .map { newWallet -> Pair(oldWallet, newWallet) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (oldWallet, newWallet) ->
                val oldSum = currentTransaction.sum
                val newSum = transaction.sum
                val amountDiff = newSum - oldSum

                if (oldWallet.id != newWallet.id) {
                    val updatedOldWallet = if (currentTransaction.isIncome) {
                        oldWallet.copy(balance = oldWallet.balance - oldSum)
                    } else {
                        oldWallet.copy(balance = oldWallet.balance + oldSum)
                    }

                    val updatedNewWallet = if (transaction.isIncome) {
                        newWallet.copy(balance = newWallet.balance + newSum)
                    } else {
                        newWallet.copy(balance = newWallet.balance - newSum)
                    }

                    updateWalletBalanceInternal(updatedOldWallet)
                    updateWalletBalanceInternal(updatedNewWallet)
                } else {
                    val updatedWallet = if (transaction.isIncome) {
                        newWallet.copy(balance = newWallet.balance + amountDiff)
                    } else {
                        newWallet.copy(balance = newWallet.balance - amountDiff)
                    }

                    if (amountDiff != 0.0) {
                        updateWalletBalanceInternal(updatedWallet)
                    }
                }

                updateTransactionInternal(transaction.copy(walletId = newWallet.id))
            }, { error ->
                Log.e(TAG, "Error getting wallets for update", error)
            })

        compositeDisposable.add(getOldWalletDisposable)
    }

    private fun updateWalletBalanceInternal(wallet: Wallet) {
        val disposable = database.walletDao().update(wallet)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(TAG, "Wallet balance updated")
            }, { error ->
                Log.e(TAG, "Error updating wallet balance", error)
            })
        compositeDisposable.add(disposable)
    }

    private fun updateTransactionInternal(transaction: Transaction) {
        val disposable = database.transactionDao().update(transaction)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _shouldCloseScreen.value = true
            }, { error ->
                Log.e(TAG, "Error updating transaction", error)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
