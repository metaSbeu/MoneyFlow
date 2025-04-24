package com.example.moneyflow.ui.fragments.home

import android.app.Application
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

    fun refreshWalletsList() {
        val disposable = database.walletDao().getWallets()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ wallets ->
                _wallets.value = wallets
                var sum = .0
                for (wallet in wallets) {
                    sum += wallet.balance
                }
                _overallBalance.value = sum
            },{
                Log.d("HomeViewModel", "refreshWalletsList: error")
            })
        compositeDisposable.add(disposable)
    }

    fun deleteWallet(wallet: Wallet) {
        val disposable = database.walletDao().delete(wallet.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                refreshWalletsList()
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}