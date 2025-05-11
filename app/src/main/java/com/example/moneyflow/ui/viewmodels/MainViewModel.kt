package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.moneyflow.data.ApiFactory.apiService
import com.example.moneyflow.utils.PreferenceManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable = CompositeDisposable()

    fun getCurrency() {
        val disposable = apiService.getCurrencyRates()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                val usdRate = response.rates["USD"]
                val eurRate = response.rates["EUR"]
                Log.d("CurrencyRates", "USD: $usdRate, EUR: $eurRate")

                // Сохраняем значения
                PreferenceManager.saveCurrencyRates(getApplication(), usdRate, eurRate)

            }, { error ->
                Log.e("CurrencyRates", "Error fetching data", error)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}