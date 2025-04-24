package com.example.moneyflow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.DefaultCategories
import com.example.moneyflow.data.DefaultWallets
import com.example.moneyflow.data.MainDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
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
        val defaultExpenseCategories = DefaultCategories.defaultExpenseCategories
        val defaultIncomeCategories = DefaultCategories.defaultIncomeCategories
        val default = defaultExpenseCategories + defaultIncomeCategories

        val disposable = database.categoryDao().getCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable { count ->
                if (count == 0) {
                    database.categoryDao().insertAll(default)
                } else {
                    Completable.complete()
                }
            }
            .subscribe()
        compositeDisposable.add(disposable)
    }

    fun insertDefaultWallets() {
        val defaultWallets = DefaultWallets.defaultWallets

        val disposable = database.walletDao().getCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable { count ->
                if (count == 0) {
                    database.walletDao().insertAll(defaultWallets)
                } else {
                    Completable.complete()
                }
            }
            .subscribe()
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    enum class PasswordState {
        EMPTY, IN_PROGRESS, COMPLETE, CORRECT, INCORRECT, ERASED_LAST_DIGIT
    }
}