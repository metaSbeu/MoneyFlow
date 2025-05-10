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
        setupBottomViewKeyboardVisibilityListener(binding.buttonAdd)

        viewModel = ViewModelProvider(this)[WalletAddViewModel::class.java]

        val adapter = BankIconAdapter(
            icons = listOf(
                "logo_sber",
                "logo_alfa",
                "logo_t_bank",
                "logo_vtb",
                "logo_raiffaisen",
                "logo_yandex",
                "logo_vk_pay",
                "logo_bitcoin",
                "logo_chelinvest",
                "logo_ozon",
                "logo_wb",
                "logo_qiwi",
                "logo_webmoney",
                "logo_paypal",
                "logo_mts",
                "logo_otkritie",
                "logo_ubrir",
                "logo_rosselhoz",
                "logo_sovkom",
                "logo_corona",
                "logo_deutsche_bank",
                "logo_pb",
                "logo_renessans",
                "logo_uralsib",
                "ic_cash"
            ),
            onIconClick = { resId ->
                selectedIconResId = resId
            }
        )

        binding.recyclerViewWalletIcons.adapter = adapter

        setupBalanceEditTextValidation()

        binding.buttonAdd.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val balanceText = binding.editTextBalance.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название кошелька", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (balanceText.isEmpty()) {
                Toast.makeText(this, "Введите баланс кошелька", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val balance = balanceText.toDoubleOrNull()
            if (balance == null) {
                Toast.makeText(
                    this,
                    "Введите корректный баланс",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedIconResId == null) {
                Toast.makeText(
                    this,
                    "Выберите иконку кошелька",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val wallet = Wallet(
                name = name,
                balance = balance,
                icon = selectedIconResId!!
            )

            viewModel.addWallet(wallet)
            viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
                if (shouldCloseScreen) {
                    finish()
                }
            }
        }
    }

    private fun setupBalanceEditTextValidation() {
        binding.editTextBalance.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

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