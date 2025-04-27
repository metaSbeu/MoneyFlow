package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityTransactionListBinding
import com.example.moneyflow.ui.adapters.TransactionAdapter
import com.example.moneyflow.ui.viewmodels.TransactionListViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class TransactionListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionListBinding
    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: TransactionListViewModel

    private var wallet: Wallet? = null
    private var startDate: Date? = null
    private var endDate: Date? = null

    private var isIncomeSelected = false
    private var isExpenseSelected = false

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

        // Обработка кнопки фильтрации по категории
        viewModel.categories.observe(this) { categories ->
            binding.buttonFilter.setOnClickListener {
                showCategoryFilterDialog(categories)
            }
        }

        // Обработка кнопки фильтрации по дате
        binding.buttonDate.setOnClickListener {
            showDateRangePicker()
        }

        binding.cardViewExpenses.setOnClickListener {
            // Если "Расходы" уже выбраны, сбрасываем фильтр
            if (isExpenseSelected) {
                resetCardViewBackgroundColor()
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.ALL)
                isExpenseSelected = false
            } else {
                // Сбрасываем фон у "Доходов", если они были выбраны
                if (isIncomeSelected) {
                    resetCardViewBackgroundColor()
                    viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.ALL)
                    isIncomeSelected = false
                }
                // Выбираем "Расходы"
                changeBackgroundColor(binding.cardViewExpenses)
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.EXPENSE)
                isExpenseSelected = true
            }
        }

        binding.cardViewIncomes.setOnClickListener {
            // Если "Доходы" уже выбраны, сбрасываем фильтр
            if (isIncomeSelected) {
                resetCardViewBackgroundColor()
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.ALL)
                isIncomeSelected = false
            } else {
                // Сбрасываем фон у "Расходов", если они были выбраны
                if (isExpenseSelected) {
                    resetCardViewBackgroundColor()
                    viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.ALL)
                    isExpenseSelected = false
                }
                // Выбираем "Доходы"
                changeBackgroundColor(binding.cardViewIncomes)
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.INCOME)
                isIncomeSelected = true
            }
        }

        setUpInsets()
    }

    private fun resetCardViewBackgroundColor() {
        binding.cardViewExpenses.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        binding.cardViewIncomes.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
    }

    private fun changeBackgroundColor(cardView: CardView) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите диапазон дат")
            .build()

        // Устанавливаем слушатель для положительной кнопки (выбора дат)
        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first ?: 0L
            val endMillis = selection.second ?: 0L

            // Преобразуем миллисекунды в даты
            startDate = Date(startMillis)
            endDate = Date(endMillis)

            updateDateTextView()
            viewModel.sortTransactionsByDate(false, startDate!!, endDate!!) // Фильтрация по дате
        }

        // Показываем MaterialDatePicker
        dateRangePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun updateDateTextView() {
        // Преобразуем startDate и endDate в строку для отображения в textView
        val startFormatted = formatDate(startDate)
        val endFormatted = formatDate(endDate)
        binding.textViewDate.text = "Период: $startFormatted - $endFormatted"
    }

    private fun formatDate(date: Date?): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return date?.let { dateFormat.format(it) } ?: ""
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
        binding.imageViewWalletIcon.setImageResource(R.drawable.ic_bank)
        binding.textViewWalletName.text = getString(R.string.all_wallets)
    }

    private fun Double.formatWithSpaces(): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault())).apply {
            groupingSize = 3
            isDecimalSeparatorAlwaysShown = false
        }
        return formatter.format(this)
    }

    private fun formatAmount(amount: Double): String {
        val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.getDefault()))
        return formatter.format(amount)
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

        fun newIntentAllWallets(context: Context): Intent {
            return Intent(context, TransactionListActivity::class.java)
        }
    }
}