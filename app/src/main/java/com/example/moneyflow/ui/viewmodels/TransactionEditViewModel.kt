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
import io.reactivex.rxjava3.core.Completable
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
    val selectedWalletId: LiveData<Int?> get() = _selectedWalletId

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
                { callback(it) },
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
                { callback(it) },
                {
                    Log.e(TAG, "Error getting wallet", it)
                    callback(null)
                }
            )
        compositeDisposable.add(disposable)
    }

    // Обновляем баланс кошелька в зависимости от транзакции
    fun updateWalletBalance(wallet: Wallet) {
        val disposable = database.walletDao().update(wallet)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Обработка успешного обновления
            }, {
                // Обработка ошибок
            })
        compositeDisposable.add(disposable)
    }

    fun getAllWallets(callback: (List<Wallet>) -> Unit) {
        val disposable = database.walletDao().getWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { callback(it) },
                {
                    Log.e(TAG, "Error getting wallets", it)
                    callback(emptyList())
                }
            )
        compositeDisposable.add(disposable)
    }

    fun deleteTransaction(transactionId: Int) {
        // 1. Получаем транзакцию для удаления
        val getTransactionDisposable = database.transactionDao().getTransactionById(transactionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ transaction ->
                // 2. Получаем связанный кошелек
                val getWalletDisposable = database.walletDao().getWalletById(transaction.walletId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ wallet ->
                        // 3. Вычисляем новый баланс
                        val newBalance = if (transaction.isIncome) {
                            wallet.balance - transaction.sum
                        } else {
                            wallet.balance + transaction.sum
                        }

                        // 4. Обновляем баланс кошелька
                        val updateWalletDisposable =
                            database.walletDao().update(wallet.copy(balance = newBalance))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    // 5. После успешного обновления баланса удаляем транзакцию
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

        // Получаем старый кошелек
        val getOldWalletDisposable = database.walletDao().getWalletById(currentTransaction.walletId)
            .subscribeOn(Schedulers.io())
            .flatMap { oldWallet ->
                // Получаем новый кошелек
                database.walletDao().getWalletById(newWalletId)
                    .map { newWallet -> Pair(oldWallet, newWallet) }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ (oldWallet, newWallet) ->
                // Обновляем балансы, если кошелек был изменен
                if (oldWallet.id != newWallet.id) {
                    val amount = currentTransaction.sum
                    // Обновляем баланс старого кошелька (возвращаем сумму)
                    val updatedOldWallet = if (currentTransaction.isIncome) {
                        oldWallet.copy(balance = oldWallet.balance - amount)
                    } else {
                        oldWallet.copy(balance = oldWallet.balance + amount)
                    }
                    updateWalletBalanceInternal(updatedOldWallet)

                    // Обновляем баланс нового кошелька (применяем сумму)
                    val updatedNewWallet = if (transaction.isIncome) {
                        newWallet.copy(balance = newWallet.balance + amount)
                    } else {
                        newWallet.copy(balance = newWallet.balance - amount)
                    }
                    updateWalletBalanceInternal(updatedNewWallet)
                }

                // Обновляем саму транзакцию
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
