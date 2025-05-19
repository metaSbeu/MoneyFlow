package com.example.moneyflow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class CategoryEditViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _shouldCloseScreen = MutableLiveData<Boolean>()
    val shouldCloseScreen: LiveData<Boolean> get() = _shouldCloseScreen

    fun updateCategory(category: Category) {
        val disposable = database.categoryDao().update(category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _shouldCloseScreen.value = true
            }
        compositeDisposable.add(disposable)
    }

    fun deleteCategory(category: Category) {
        val disposable = database.categoryDao().delete(category.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _shouldCloseScreen.value = true
            }
        compositeDisposable.add(disposable)
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}