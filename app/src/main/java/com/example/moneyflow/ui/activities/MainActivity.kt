package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.ui.adapters.WalletAdapter
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityMainBinding
import com.example.moneyflow.ui.fragments.home.HomeFragment
import com.example.moneyflow.ui.fragments.planning.PlanningFragment
import com.example.moneyflow.ui.fragments.settings.SettingsFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WalletAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpInsets()
        adapter = WalletAdapter()
//        binding.recyclerViewWallets.adapter = adapter
        adapter.wallets = testWallets()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

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

    fun testWallets(): List<Wallet> {
        val wallets = mutableListOf<Wallet>()
        repeat(1000) {
            val wallet = Wallet("Wallet ${it + 1}: ", it * 1000)
            wallets.add(wallet)
        }
        return wallets
    }

    private fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            return intent
        }
    }
}