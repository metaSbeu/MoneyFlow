package com.example.moneyflow.ui.fragments.planning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Plan
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class PlanningViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _plans = MutableLiveData<List<Plan>>()
    val plans: LiveData<List<Plan>> = _plans

    private val _monthSum = MutableLiveData<Double>()
    val monthSum: LiveData<Double> = _monthSum

    fun refreshPlans() {
        val disposable = database.planDao().getPlans().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                _plans.value = it
                getMonthSum()
            }, {
                it.printStackTrace()
            })
        compositeDisposable.add(disposable)
    }

    fun setNotificationActive(plan: Plan, isActive: Boolean) {
        val disposable = database.planDao().update(plan.copy(isNotificationActive = isActive))
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {

            }
        compositeDisposable.add(disposable)
    }

    fun getMonthSum() {
        val disposable = database.planDao().getPlans().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe { plans ->
                val monthSum = plans.sumOf { it.sum }
                _monthSum.value = monthSum
            }
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}