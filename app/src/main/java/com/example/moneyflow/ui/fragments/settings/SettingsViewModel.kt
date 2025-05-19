package com.example.moneyflow.ui.fragments.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.moneyflow.utils.PreferenceManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _currency = MutableLiveData<String>()
    val currency: LiveData<String> get() = _currency

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> get() = _theme

    init {
        _currency.value = PreferenceManager.getSelectedCurrency(context)
        _theme.value = PreferenceManager.getSelectedTheme(context)
    }

    fun setCurrency(value: String) {
        PreferenceManager.setSelectedCurrency(context, value)
        _currency.value = value
    }

    fun setTheme(value: String) {
        PreferenceManager.setSelectedTheme(context, value)
        _theme.value = value
    }
}
