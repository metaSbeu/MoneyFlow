package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel.Companion.TAG
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val CORRECT_PASSWORD = "0000"
        const val PASSWORD_LENGTH = 4
    }

    private val database = MainDatabase.getDb(application)
    private val compositeDisposable = CompositeDisposable()

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _passwordState = MutableLiveData<PasswordState>()
    val passwordState: LiveData<PasswordState> get() = _passwordState

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> get() = _navigateToMain

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean> get() = _showError

    init {
        _password.value = ""
        _passwordState.value = PasswordState.EMPTY
    }

    fun addDigit(digit: Int) {
        val current = _password.value ?: ""
        if (current.length < PASSWORD_LENGTH) {
            _password.value = current + digit
            updatePasswordState()
        }
    }

    fun removeLastDigit() {
        val current = _password.value ?: ""
        if (current.isNotEmpty()) {
            _password.value = current.dropLast(1)
            _passwordState.value = PasswordState.ERASED_LAST_DIGIT
        } else {
            _passwordState.value = PasswordState.EMPTY
        }
    }

    fun clearPassword() {
        _password.value = ""
        _passwordState.value = PasswordState.EMPTY
    }

    fun checkPassword() {
        if (_password.value == CORRECT_PASSWORD) {
            _passwordState.value = PasswordState.CORRECT
            _navigateToMain.value = true
        } else {
            _passwordState.value = PasswordState.INCORRECT
            _showError.value = true
        }
    }

    fun onNavigationComplete() {
        _navigateToMain.value = false
    }

    fun onErrorShown() {
        _showError.value = false
    }

    private fun updatePasswordState() {
        _passwordState.value = when (_password.value?.length ?: 0) {
            0 -> PasswordState.EMPTY
            PASSWORD_LENGTH -> PasswordState.COMPLETE
            else -> PasswordState.IN_PROGRESS
        }
    }

    fun insertDefaultCategories() {
        val defaultCategories = listOf(
            Category(name = "Здоровье", iconResId = R.drawable.ic_health, isIncome = false),
            Category(name = "Развлечения", iconResId = R.drawable.ic_leisure, isIncome = false),
            Category(name = "Дом", iconResId = R.drawable.ic_home, isIncome = false),
            Category(name = "Кафе", iconResId = R.drawable.ic_cafe, isIncome = false),
            Category(name = "Образование", iconResId = R.drawable.ic_education, isIncome = false),
            Category(name = "Подарки", iconResId = R.drawable.ic_gift, isIncome = false),
            Category(name = "Продукты", iconResId = R.drawable.ic_grocery, isIncome = false)
        )
        val count = database.categoryDao().getCount()
        if (count == 0) {
            val disposable = database.categoryDao().insertAll(defaultCategories)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {

                    },
                    {
                        Log.d(TAG, it.toString())
                    })
            compositeDisposable.add(disposable)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    enum class PasswordState {
        EMPTY, IN_PROGRESS, COMPLETE, CORRECT, INCORRECT, ERASED_LAST_DIGIT
    }
}