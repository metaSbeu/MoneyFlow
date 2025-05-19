package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.databinding.ActivityMainBinding
import com.example.moneyflow.ui.fragments.home.HomeFragment
import com.example.moneyflow.ui.fragments.planning.PlanningFragment
import com.example.moneyflow.ui.fragments.settings.SettingsFragment
import com.example.moneyflow.ui.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включаем поддержку edge-to-edge и отключаем автоматические отступы
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setUpInsets()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigationHome -> HomeFragment()
                R.id.navigationPlanning -> PlanningFragment()
                R.id.navigationSettings -> SettingsFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
                true
            } == true
        }
    }

    private fun setUpInsets() {
        // Устанавливаем отступы вручную
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Применяем отступы к основному контейнеру (кроме bottom)
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )

            // Применяем отступ снизу только к нижней навигации
            binding.bottomNavigationView.setPadding(
                binding.bottomNavigationView.paddingLeft,
                binding.bottomNavigationView.paddingTop,
                binding.bottomNavigationView.paddingRight,
                systemBars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
