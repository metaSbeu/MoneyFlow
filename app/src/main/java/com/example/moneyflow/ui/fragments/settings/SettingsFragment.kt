package com.example.moneyflow.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.moneyflow.R
import com.example.moneyflow.databinding.FragmentSettingsBinding
import com.example.moneyflow.ui.activities.AuthActivity
import com.example.moneyflow.ui.activities.NotificationsSettingsActivity
import com.example.moneyflow.utils.PreferenceManager

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Изменение PIN
        binding.cardViewChangePIN.setOnClickListener {
            val intent = AuthActivity.newIntent(requireContext(), AuthActivity.MODE_CHANGE_PIN)
            startActivity(intent)
        }

        // Настройка спиннера выбора валюты
        val currencies = resources.getStringArray(R.array.currencies)
        val currentCurrency = PreferenceManager.getSelectedCurrency(requireContext())
        val selectedIndex = currencies.indexOf(currentCurrency).takeIf { it >= 0 } ?: 0
        binding.currencySpinner.setSelection(selectedIndex)

        binding.currencySpinner.setOnItemSelectedListener(object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencies[position]
                PreferenceManager.setSelectedCurrency(requireContext(), selectedCurrency)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // Ничего не делать
            }
        })

        binding.cardViewNotifications.setOnClickListener {
            val intent = Intent(requireContext(), NotificationsSettingsActivity::class.java)
            startActivity(intent)
        }


        val themes = resources.getStringArray(R.array.themes)
        val currentTheme = PreferenceManager.getSelectedTheme(requireContext())
        val currentIndex = themes.indexOf(currentTheme).takeIf { it >= 0 } ?: 0
        binding.themeSpinner.setSelection(currentIndex)

        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedTheme = themes[position]
                PreferenceManager.setSelectedTheme(requireContext(), selectedTheme)

                when (selectedTheme) {
                    "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}