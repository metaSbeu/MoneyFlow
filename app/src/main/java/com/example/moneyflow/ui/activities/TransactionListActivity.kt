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
import com.example.moneyflow.databinding.ActivityTransactionListBinding
import com.example.moneyflow.ui.adapters.TransactionAdapter
import com.example.moneyflow.ui.viewmodels.TransactionListViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class TransactionListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionListBinding
    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: TransactionListViewModel

    private var wallet: Wallet? = null // Теперь nullable!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[TransactionListViewModel::class.java]

        wallet = intent.getSerializableExtra(EXTRA_WALLET) as? Wallet

        if (wallet != null) {
            setupWallet(wallet!!)
            viewModel.loadTransactions(wallet!!.id)
        } else {
            setupAllWallets()
            viewModel.loadTransactions()
        }

        adapter = TransactionAdapter { transaction ->
            val transactionId = transaction.id
            val intent = TransactionEditActivity.newIntent(this, transactionId)
            startActivity(intent)
        }

        observeViewModels()
        binding.recyclerViewTransactions.adapter = adapter

        viewModel.categories.observe(this) { categories ->
            binding.buttonFilter.setOnClickListener {
                showCategoryFilterDialog(categories)
            }
        }

        setUpInsets()
    }

    private fun showCategoryFilterDialog(categories: List<String>) {
        val updatedCategories = listOf("Все категории") + categories

        val categoryArray = updatedCategories.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Выберите категорию")
            .setItems(categoryArray) { _, which ->
                val selectedCategory = updatedCategories[which]
                viewModel.filterTransactionsByCategory(selectedCategory)
            }
            .show()
    }

    private fun setupWallet(wallet: Wallet) {
        binding.imageViewWalletIcon.setImageResource(wallet.iconResId)
        val formatted = wallet.balance.formatWithSpaces()
        binding.textViewWalletName.text = getString(R.string.wallet_main_info, wallet.name, formatted)
    }

    private fun setupAllWallets() {
        binding.imageViewWalletIcon.setImageResource(R.drawable.ic_bank) // Поставь сюда иконку "все счета"
        binding.textViewWalletName.text = getString(R.string.all_wallets)
    }

    private fun Double.formatWithSpaces(): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault())).apply {
            groupingSize = 3
            isDecimalSeparatorAlwaysShown = false
        }
        return formatter.format(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions(wallet?.id)
    }

    private fun observeViewModels() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.transactions = transactions
        }
    }

    private fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        const val EXTRA_WALLET = "wallet"

        fun newIntent(context: Context, wallet: Wallet): Intent {
            val intent = Intent(context, TransactionListActivity::class.java)
            intent.putExtra(EXTRA_WALLET, wallet)
            return intent
        }

        fun newIntentAllWallets(context: Context): Intent { // второй метод
            return Intent(context, TransactionListActivity::class.java)
        }
    }
}
