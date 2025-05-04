package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityWalletEditBinding
import com.example.moneyflow.ui.adapters.BankIconAdapter
import com.example.moneyflow.ui.viewmodels.WalletEditViewModel
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener

class WalletEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletEditBinding
    private lateinit var adapter: BankIconAdapter
    private lateinit var viewModel: WalletEditViewModel
    private var selectedIconResId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWalletEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomViewKeyboardVisibilityListener(binding.bottomButtonsContainer)
        setupInsets()
        viewModel = ViewModelProvider(this)[WalletEditViewModel::class.java]

        val wallet = intent.getSerializableExtra(EXTRA_WALLET) as Wallet

        adapter = BankIconAdapter(
            icons = listOf("logo_sber", "logo_alfa", "logo_t_bank"),
            onIconClick = { resId ->
                selectedIconResId = resId
            })
        binding.recyclerViewWalletIcons.adapter = adapter

        binding.editTextNewName.setText(wallet.name.toString())

        binding.buttonSave.setOnClickListener {
            val newName = binding.editTextNewName.text.toString()
            val walletToReplace = Wallet(
                id = wallet.id,
                name = newName,
                balance = wallet.balance,
                icon = selectedIconResId!!
            )
            viewModel.editWallet(walletToReplace)
        }

        viewModel.shouldCloseScreen.observe(this) {
            if (it) {
                finish()
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
        const val EXTRA_WALLET = "wallet"

        fun newIntent(context: Context, wallet: Wallet): Intent {
            val intent = Intent(context, WalletEditActivity::class.java)
            intent.putExtra(EXTRA_WALLET, wallet)
            return intent
        }
    }
}