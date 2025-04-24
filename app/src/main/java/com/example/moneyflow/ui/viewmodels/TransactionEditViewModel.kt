package com.example.moneyflow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.Wallet
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

    fun getTransactionById(transactionId: Int) {
        val disposable = database.transactionDao().getTransactionById(transactionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _transaction.value = it
                },
                {
                    // Обработка ошибок
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

    // Обновление транзакции и кошелька
    fun updateTransaction(transaction: Transaction) {
        val walletDisposable = database.walletDao().getWalletById(transaction.walletId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ wallet ->
                // Получаем старую транзакцию, чтобы вычесть её сумму из баланса
                val oldTransaction = _transaction.value
                val oldAmount = oldTransaction?.sum ?: 0.0

                // Вычисляем новый баланс
                val newBalance = if (transaction.isIncome) {
                    wallet.balance - oldAmount + transaction.sum // Вычитаем старую сумму и добавляем новую
                } else {
                    wallet.balance + oldAmount - transaction.sum // Вычитаем старую сумму и вычитаем новую
                }

                val updatedWallet = wallet.copy(balance = newBalance)
                updateWalletBalance(updatedWallet)

                // Обновляем транзакцию в базе данных
                val transactionDisposable = database.transactionDao().update(transaction)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _shouldCloseScreen.value = true
                    }, {
                        // Обработка ошибок
                    })
                compositeDisposable.add(transactionDisposable)
            }, {
                // Обработка ошибки, если не найден кошелек
            })
        compositeDisposable.add(walletDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
