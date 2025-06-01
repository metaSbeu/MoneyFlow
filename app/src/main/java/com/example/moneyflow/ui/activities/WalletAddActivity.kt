package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityWalletAddBinding
import com.example.moneyflow.ui.adapters.BankIconAdapter
import com.example.moneyflow.ui.viewmodels.WalletAddViewModel
import com.example.moneyflow.utils.DefaultWalletIcons
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener

class WalletAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletAddBinding
    private lateinit var viewModel: WalletAddViewModel

    private var selectedIconResId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWalletAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        setupBottomViewKeyboardVisibilityListener(binding.buttonSave)

        viewModel = ViewModelProvider(this)[WalletAddViewModel::class.java]

        val adapter = BankIconAdapter(
            icons = DefaultWalletIcons.all, onIconClick = { resId ->
                selectedIconResId = resId
            })

        binding.recyclerViewWalletIcons.adapter = adapter

        setupBalanceEditTextValidation()

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val balanceText = binding.editTextBalance.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.type_wallet_name), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (balanceText.isEmpty()) {
                Toast.makeText(this, getString(R.string.type_wallet_balance), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val balance = balanceText.toDoubleOrNull()
            if (balance == null) {
                Toast.makeText(
                    this, getString(R.string.type_correct_balance), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedIconResId == null) {
                Toast.makeText(
                    this, getString(R.string.choose_wallet_icon), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val wallet = Wallet(
                name = name, balance = balance, icon = selectedIconResId!!
            )

            viewModel.addWallet(wallet)
            viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
                if (shouldCloseScreen) {
                    finish()
                }
            }
        }

        binding.buttonFinish.setOnClickListener {
            finish()
        }
    }

    private fun setupBalanceEditTextValidation() {
        binding.editTextBalance.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                s?.let { editable ->
                    val text = editable.toString()
                    if (text.startsWith("0") && text.length > 1 && !text.startsWith("0.")) {
                        editable.delete(0, 1)
                    }
                    if (text == ".") {
                        editable.clear()
                    }
                }
            }
        })
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
            val intent = Intent(context, WalletAddActivity::class.java)
            return intent
        }
    }
}