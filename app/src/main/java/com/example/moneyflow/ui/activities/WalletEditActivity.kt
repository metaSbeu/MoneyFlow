package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private lateinit var initialWallet: Wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWalletEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        viewModel = ViewModelProvider(this)[WalletEditViewModel::class.java]

        initialWallet = intent.getSerializableExtra(EXTRA_WALLET) as Wallet
        selectedIconResId = initialWallet.icon // Инициализируем выбранную иконку

        adapter = BankIconAdapter(
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
            },
            preselectedIcon = initialWallet.icon // Передаем предустановленную иконку адаптеру
        )
        binding.recyclerViewWalletIcons.adapter = adapter

        binding.editTextNewName.setText(initialWallet.name.toString())

        binding.buttonSave.setOnClickListener {
            val newName = binding.editTextNewName.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Введите название кошелька", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedIconResId == null) {
                Toast.makeText(this, "Выберите иконку кошелька", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val walletToReplace = Wallet(
                id = initialWallet.id,
                name = newName,
                balance = initialWallet.balance,
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