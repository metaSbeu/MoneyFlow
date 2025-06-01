package com.example.moneyflow.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.moneyflow.R
import com.example.moneyflow.databinding.FragmentSettingsBinding
import com.example.moneyflow.ui.activities.AuthActivity
import com.example.moneyflow.ui.activities.NotificationsSettingsActivity

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardViewChangePIN.setOnClickListener {
            val intent = AuthActivity.newIntent(requireContext(), AuthActivity.MODE_CHANGE_PIN)
            startActivity(intent)
        }

        val currencies = resources.getStringArray(R.array.currencies)
        viewModel.currency.observe(viewLifecycleOwner) { currentCurrency ->
            val index = currencies.indexOf(currentCurrency).takeIf { it >= 0 } ?: 0
            binding.currencySpinner.setSelection(index)
        }

        binding.currencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    viewModel.setCurrency(currencies[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        binding.cardViewNotifications.setOnClickListener {
            val intent = Intent(requireContext(), NotificationsSettingsActivity::class.java)
            startActivity(intent)
        }

        val themes = resources.getStringArray(R.array.themes)
        viewModel.theme.observe(viewLifecycleOwner) { currentTheme ->
            val index = themes.indexOf(currentTheme).takeIf { it >= 0 } ?: 0
            binding.themeSpinner.setSelection(index)
            applyTheme(currentTheme)
        }

        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                viewModel.setTheme(themes[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            getString(R.string.theme_system_default) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
            getString(R.string.theme_light) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            getString(R.string.theme_dark) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }
}