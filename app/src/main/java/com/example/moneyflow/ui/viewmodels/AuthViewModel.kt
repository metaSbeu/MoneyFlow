package com.example.moneyflow.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.data.ApiFactory.apiService
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.utils.DefaultCategories
import com.example.moneyflow.utils.DefaultWallets
import com.example.moneyflow.utils.PreferenceManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
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

    private val _setupState = MutableLiveData<SetupState>(SetupState.FIRST_ENTRY)
    val setupState: LiveData<SetupState> get() = _setupState

    private var firstPinAttempt: String = ""

    private val _changePinState = MutableLiveData<ChangePinState>(ChangePinState.IDLE)
    val changePinState: LiveData<ChangePinState> get() = _changePinState

    private var newPinAttempt: String = ""

    init {
        _password.value = ""
        _passwordState.value = PasswordState.EMPTY
    }

    fun getCurrency() {
        val disposable = apiService.getCurrencyRates()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
                val usdRate = response.rates["USD"]
                val eurRate = response.rates["EUR"]
                Log.d("CurrencyRates", "USD: $usdRate, EUR: $eurRate")

                PreferenceManager.saveCurrencyRates(getApplication(), usdRate, eurRate)

            }, { error ->
                Log.e("CurrencyRates", "Error fetching data", error)
            })
        compositeDisposable.add(disposable)
    }

    fun setSetupModeFirstEntry() {
        _setupState.value = SetupState.FIRST_ENTRY
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

    fun checkPassword(context: Context) {
        if (_password.value == PreferenceManager.getPinCode(context)) {
            _passwordState.value = PasswordState.CORRECT
            _navigateToMain.value = true
        } else {
            _passwordState.value = PasswordState.INCORRECT
            _showError.value = true
        }
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

    fun insertDefaultCategories(context: Context) {
        val defaultExpenseCategories = DefaultCategories.getDefaultExpenseCategories(context)
        val defaultIncomeCategories = DefaultCategories.getDefaultIncomeCategories(context)
        val default = defaultExpenseCategories + defaultIncomeCategories

        val disposable = database.categoryDao().getCount().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).flatMapCompletable { count ->
                if (count == 0) {
                    database.categoryDao().insertAll(default)
                } else {
                    Completable.complete()
                }
            }.subscribe()
        compositeDisposable.add(disposable)
    }

    fun insertDefaultWallets(context: Context) {
        val defaultWallets = DefaultWallets.getDefaultWallets(context)

        val disposable = database.walletDao().getCount().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).flatMapCompletable { count ->
                if (count == 0) {
                    database.walletDao().insertAll(defaultWallets)
                } else {
                    Completable.complete()
                }
            }.subscribe()
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun handleSetupMode(pin: String) {
        when (_setupState.value) {
            SetupState.FIRST_ENTRY -> {
                firstPinAttempt = pin
                _setupState.value = SetupState.CONFIRMATION
                _password.value = ""
                _passwordState.value = PasswordState.EMPTY
            }

            SetupState.CONFIRMATION -> {
                if (pin == firstPinAttempt) {
                    PreferenceManager.setPinCode(getApplication(), pin)
                    PreferenceManager.setFirstLaunch(getApplication(), false)
                    _navigateToMain.value = true
                } else {
                    _setupState.value = SetupState.FIRST_ENTRY
                    _passwordState.value = PasswordState.INCORRECT
                    _showError.value = true
                    firstPinAttempt = ""
                }
            }

            else -> Unit
        }
    }

    fun setChangePinMode() {
        _changePinState.value = ChangePinState.ENTER_OLD_PIN
        _password.value = ""
        _passwordState.value = PasswordState.EMPTY
    }

    fun handlePinChange(context: Context) {
        when (_changePinState.value) {
            ChangePinState.ENTER_OLD_PIN -> {
                if (_password.value?.length == PASSWORD_LENGTH) {
                    if (_password.value == PreferenceManager.getPinCode(context)) {
                        _changePinState.value = ChangePinState.ENTER_NEW_PIN
                        _password.value = ""
                        _passwordState.value = PasswordState.EMPTY
                    } else {
                        _passwordState.value = PasswordState.INCORRECT
                        _showError.value = true
                        _password.value = ""
                        _passwordState.value = PasswordState.EMPTY
                    }
                }
            }

            ChangePinState.ENTER_NEW_PIN -> {
                if (_password.value?.length == PASSWORD_LENGTH) {
                    newPinAttempt = _password.value!!
                    _changePinState.value = ChangePinState.CONFIRM_NEW_PIN
                    _password.value = ""
                    _passwordState.value = PasswordState.EMPTY
                }
            }

            ChangePinState.CONFIRM_NEW_PIN -> {
                if (_password.value?.length == PASSWORD_LENGTH) {
                    if (_password.value == newPinAttempt) {
                        PreferenceManager.setPinCode(getApplication(), newPinAttempt)
                        _changePinState.value = ChangePinState.SUCCESS
                        _navigateToMain.value = true
                    } else {
                        _passwordState.value = PasswordState.INCORRECT
                        _showError.value = true
                        _password.value = ""
                        _passwordState.value = PasswordState.EMPTY
                        _changePinState.value = ChangePinState.ENTER_NEW_PIN
                    }
                }
            }

            else -> Unit
        }
    }

    enum class SetupState {
        FIRST_ENTRY, CONFIRMATION
    }

    enum class PasswordState {
        EMPTY, IN_PROGRESS, COMPLETE, CORRECT, INCORRECT, ERASED_LAST_DIGIT
    }

    enum class ChangePinState {
        IDLE, ENTER_OLD_PIN, ENTER_NEW_PIN, CONFIRM_NEW_PIN, SUCCESS
    }
}
