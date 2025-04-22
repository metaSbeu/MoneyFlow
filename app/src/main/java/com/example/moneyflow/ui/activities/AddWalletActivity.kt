package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.moneyflow.R
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityAddWalletBinding
import com.example.moneyflow.ui.adapters.BankIconAdapter
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.adapters.WalletAdapter
import com.example.moneyflow.ui.viewmodels.AddWalletViewModel

class AddWalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWalletBinding
    private lateinit var viewModel: AddWalletViewModel

    private var selectedIconResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        viewModel = ViewModelProvider(this)[AddWalletViewModel::class.java]

        val adapter = BankIconAdapter(
            icons = listOf(R.drawable.logo_sber, R.drawable.logo_alfa, R.drawable.logo_t_bank),
            onIconClick = { resId ->
                selectedIconResId = resId
            }
        )

        binding.recyclerViewWalletIcons.adapter = adapter

        binding.buttonAdd.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val balanceText = binding.editTextBalance.text.toString().trim()

            if (name.isEmpty() || balanceText.isEmpty() || selectedIconResId == null) {
                Toast.makeText(
                    this,
                    "Выберите иконку и заполните все поля",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val wallet = Wallet(
                name = name,
                balance = balanceText.toInt(),
                iconResId = selectedIconResId!!,
                type = "Cash"
            )

            viewModel.addWallet(wallet)
            viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
                if (shouldCloseScreen) {
                    finish()
                }
            }
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