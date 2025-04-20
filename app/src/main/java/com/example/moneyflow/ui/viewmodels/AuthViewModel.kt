package com.example.moneyflow.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val CORRECT_PASSWORD = "0000"
        const val PASSWORD_LENGTH = 4
    }

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

    enum class PasswordState {
        EMPTY, IN_PROGRESS, COMPLETE, CORRECT, INCORRECT, ERASED_LAST_DIGIT
    }
}