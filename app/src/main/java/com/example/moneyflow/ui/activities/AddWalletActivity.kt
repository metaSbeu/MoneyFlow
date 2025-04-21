package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moneyflow.R
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityAddWalletBinding
import com.example.moneyflow.ui.adapters.BankIconAdapter
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.adapters.WalletAdapter

class AddWalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWalletBinding
    private lateinit var database: MainDatabase

    private var selectedIcon: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        database = MainDatabase.getDb(application)

        val adapter = BankIconAdapter(
            icons = listOf("logo_sber", "logo_alfa", "logo_t_bank"),
            onIconClick = { selectedIconName ->
                selectedIcon = selectedIconName
            }
        )
        binding.recyclerViewWalletIcons.adapter = adapter

        binding.buttonAdd.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val balance = binding.editTextBalance.text.toString().trim().toInt()
            val wallet = Wallet(name = name, balance = balance, icon = selectedIcon!!)

            database.walletDao().insert(wallet)
            finish()
        }
    }

    fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, AddWalletActivity::class.java)
            return intent
        }
    }
}