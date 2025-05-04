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

class CategoryAddViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _shouldCloseScreen = MutableLiveData<Boolean>()
    val shouldCloseScreen: LiveData<Boolean> get() = _shouldCloseScreen

    private val _defaultIcons = MutableLiveData<List<Category>>()
    val defaultIcons: LiveData<List<Category>> get() = _defaultIcons

    init {
        _defaultIcons.value = getDefaultCategoryIcons()
    }

    fun addCategory(category: Category) {
        val disposable = database.categoryDao().insert(category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _shouldCloseScreen.value = true
                }
            )
        compositeDisposable.add(disposable)
    }

    fun getDefaultCategoryIcons(): List<Category> {
        return listOf<Category>(
            Category(name = "", icon = "ic_movie", isIncome = false),
            Category(name = "", icon = "ic_game", isIncome = false),
            Category(name = "", icon = "ic_sport", isIncome = false),
            Category(name = "", icon = "ic_food", isIncome = false),
            Category(name = "", icon = "ic_pet", isIncome = false),
            Category(name = "", icon = "ic_fruit", isIncome = false),
            Category(name = "", icon = "ic_clothes", isIncome = false),
            Category(name = "", icon = "ic_shoe", isIncome = false),
            Category(name = "", icon = "ic_diamond", isIncome = false),
            Category(name = "", icon = "ic_furniture", isIncome = false),
            Category(name = "", icon = "ic_music", isIncome = false),
            Category(name = "", icon = "ic_computer", isIncome = false),
            Category(name = "", icon = "ic_bicycle", isIncome = false),
            Category(name = "", icon = "ic_wifi", isIncome = false),
            Category(name = "", icon = "ic_car", isIncome = false),
            Category(name = "", icon = "ic_plane", isIncome = false),
            Category(name = "", icon = "ic_tooth", isIncome = false),
            Category(name = "", icon = "ic_finance", isIncome = false),
            Category(name = "", icon = "ic_family", isIncome = false),
            Category(name = "", icon = "ic_train_bus", isIncome = false),
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}