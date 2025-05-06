package com.example.moneyflow.ui.fragments.home

import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Wallet
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _wallets = MutableLiveData<List<Wallet>>()
    val wallets: LiveData<List<Wallet>> get() = _wallets

    private val _overallBalance = MutableLiveData<Double>()
    val overallBalance: LiveData<Double> get() = _overallBalance

    private val _monthExpenses = MutableLiveData<Double>()
    val monthExpenses: LiveData<Double> get() = _monthExpenses

    private val _monthIncomes = MutableLiveData<Double>()
    val monthIncomes: LiveData<Double> get() = _monthIncomes

    private val _wallet = MutableLiveData<Wallet?>() // Изменили на MutableLiveData<Wallet?>
    val wallet: LiveData<Wallet?> get() = _wallet // Добавили публичный getter

    fun getMonthBalance() {
        val disposable = database.transactionDao().getTransactions().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ transactions ->
                var expSum = 0.0
                var incSum = 0.0
                for (transaction in transactions) {
                    if (isInCurrentMonth(transaction.createdAt)) {
                        when (transaction.isIncome) {
                            true -> incSum += transaction.sum
                            false -> expSum += transaction.sum
                        }
                    }
                }
                _monthExpenses.value = expSum
                _monthIncomes.value = incSum
            })
        compositeDisposable.add(disposable)
    }

    fun isInCurrentMonth(createdAt: Long): Boolean {
        val calendar = Calendar.getInstance()

        // Начало месяца
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        // Конец месяца
        calendar.add(Calendar.MONTH, 1) // Перейти на следующий месяц
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Первый день следующего месяца
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfNextMonth = calendar.timeInMillis
        return createdAt in startOfMonth until startOfNextMonth
    }

    fun refreshWalletsList() {
        val disposable = database.walletDao().getWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ wallets ->
                _wallets.value = wallets
                var sum = 0.0
                for (wallet in wallets) {
                    sum += wallet.balance
                }
                _overallBalance.value = sum

                getMonthBalance()
            }, {
                Log.d("HomeViewModel", "refreshWalletsList: error")
            })
        compositeDisposable.add(disposable)
    }

    fun deleteWallet(wallet: Wallet) {
        val disposable = database.walletDao().delete(wallet.id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                refreshWalletsList()
            })
        compositeDisposable.add(disposable)
    }

    fun getWalletById(walletId: Int): LiveData<Wallet?> { // Изменили возвращаемый тип на LiveData<Wallet?>
        val disposable = database.walletDao().getWalletById(walletId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ wallet ->
                _wallet.value = wallet // Обновляем _wallet при получении данных
            }, { error ->
                Log.e("HomeViewModel", "Error fetching wallet by ID: $walletId", error)
                _wallet.value = null // Обрабатываем ошибку, устанавливая значение null
            })
        compositeDisposable.add(disposable)
        return _wallet // Возвращаем LiveData
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}