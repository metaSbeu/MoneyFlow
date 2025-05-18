package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.TransactionWithCategory
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.ActivityTransactionListBinding
import com.example.moneyflow.ui.adapters.TransactionAdapter
import com.example.moneyflow.ui.viewmodels.TransactionListViewModel
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.utils.IconResolver
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
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

        intent.getStringExtra(EXTRA_FILTER_TYPE)?.let { filterType ->
            when (filterType) {
                FILTER_INCOME -> {
                    changeBackgroundColor(binding.cardViewIncomes)
                    viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.INCOME)
                    isIncomeSelected = true
                }

                FILTER_EXPENSE -> {
                    changeBackgroundColor(binding.cardViewExpenses)
                    viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.EXPENSE)
                    isExpenseSelected = true
                }
            }
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

        binding.buttonDate.setOnClickListener {
            showDateRangePicker()
        }

        binding.cardViewExpenses.setOnClickListener {
            if (isExpenseSelected) {
                resetCardViewBackgroundColor()
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.ALL)
                isExpenseSelected = false
            } else {
                if (isIncomeSelected) {
                    resetCardViewBackgroundColor()
                    isIncomeSelected = false
                }
                changeBackgroundColor(binding.cardViewExpenses)
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.EXPENSE)
                isExpenseSelected = true
            }
        }

        binding.cardViewIncomes.setOnClickListener {
            if (isIncomeSelected) {
                resetCardViewBackgroundColor()
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.ALL)
                isIncomeSelected = false
            } else {
                if (isExpenseSelected) {
                    resetCardViewBackgroundColor()
                    isExpenseSelected = false
                }
                changeBackgroundColor(binding.cardViewIncomes)
                viewModel.filterTransactionsByType(TransactionListViewModel.FilterType.INCOME)
                isIncomeSelected = true
            }
        }
        setUpInsets()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions(wallet?.id)
        wallet?.let {
            viewModel.refreshWallet(it)
        }
    }

    private fun setupPieChart(transactions: List<TransactionWithCategory>) {
        val pieChart = binding.pieChart

        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setNoDataText(getString(R.string.piechart_not_available))
        pieChart.setHoleColor(ContextCompat.getColor(this, R.color.background))
        pieChart.setDrawCenterText(true)
        pieChart.legend.isEnabled = false

        val entries = preparePieChartData(transactions)
        val labelTextColorMF = ContextCompat.getColor(this, R.color.text_color_primary)
        val valueTextColorMF = ContextCompat.getColor(this, R.color.text_color_primary)
        pieChart.setEntryLabelColor(labelTextColorMF)

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = getString(R.string.no_data)
            return
        }

        val dataSet = PieDataSet(entries, getString(R.string.transaction_categories)).apply {
            sliceSpace = 2f
            colors = getPieChartColors()
            valueTextSize = 12f
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f
            valueTextColor = valueTextColorMF
            valueLineColor =
                ContextCompat.getColor(this@TransactionListActivity, R.color.item_background)
            valueLineWidth = 1f
        }

        val pieData = PieData(dataSet)
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toDouble().formatWithSpaces(this@TransactionListActivity)
            }
        }

        pieChart.data = pieData

        pieChart.animateY(1000, Easing.EaseInOutQuad)
        pieChart.invalidate()
    }

    private fun getPieChartColors(): List<Int> {
        return listOf(
            ContextCompat.getColor(this, R.color.category_color_1),
            ContextCompat.getColor(this, R.color.category_color_6),
            ContextCompat.getColor(this, R.color.category_color_10),
            ContextCompat.getColor(this, R.color.category_color_12),
            ContextCompat.getColor(this, R.color.category_color_3),
            ContextCompat.getColor(this, R.color.category_color_9),
            ContextCompat.getColor(this, R.color.category_color_15),
            ContextCompat.getColor(this, R.color.category_color_5),
            ContextCompat.getColor(this, R.color.category_color_16),
            ContextCompat.getColor(this, R.color.category_color_4),
            ContextCompat.getColor(this, R.color.category_color_1)
        )
    }

    private fun preparePieChartData(transactions: List<TransactionWithCategory>): List<PieEntry> {
        val categorySums = mutableMapOf<String, Double>()
        for (transaction in transactions) {
            val categoryName = transaction.category.name
            val amount = transaction.transaction.sum
            categorySums[categoryName] = categorySums.getOrDefault(categoryName, 0.0) + amount
        }
        return categorySums.map { PieEntry(it.value.toFloat(), it.key) }
    }

    private fun resetCardViewBackgroundColor() {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.cardBackgroundColor, typedValue, true)
        val backgroundColor = typedValue.data
        binding.cardViewExpenses.setCardBackgroundColor(backgroundColor)
        binding.cardViewIncomes.setCardBackgroundColor(backgroundColor)
    }

    private fun changeBackgroundColor(cardView: CardView) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.choose_date_range)).build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first ?: 0L
            val endMillis = selection.second ?: 0L

            startDate = Date(startMillis)
            endDate = Date(endMillis)

            updateDateTextView()
            viewModel.sortTransactionsByDate(startDate!!, endDate!!)
        }

        dateRangePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun updateDateTextView() {
        val startFormatted = formatDate(startDate)
        val endFormatted = formatDate(endDate)
        binding.textViewDate.text = getString(R.string.period, startFormatted, endFormatted)
    }

    private fun formatDate(date: Date?): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return date?.let { dateFormat.format(it) } ?: ""
    }

    private fun showCategoryFilterDialog(categories: List<String>) {
        val updatedCategories = listOf(getString(R.string.all_categories)) + categories
        val categoryArray = updatedCategories.toTypedArray()

        AlertDialog.Builder(this).setTitle(getString(R.string.choose_category))
            .setItems(categoryArray) { _, which ->
                val selectedCategory = updatedCategories[which]
                viewModel.filterTransactionsByCategory(selectedCategory, this)
            }.show()
    }

    private fun setupWallet(wallet: Wallet) {
        val iconResId = IconResolver.resolve(wallet.icon)
        binding.imageViewWalletIcon.setImageResource(iconResId)
        val formatted = wallet.balance.formatWithSpaces(this)
        binding.textViewWalletName.text =
            getString(R.string.wallet_main_info, wallet.name, formatted)
    }

    private fun setupAllWallets() {
        binding.imageViewWalletIcon.setImageResource(R.drawable.ic_bank)

        val iconColor = ContextCompat.getColor(this, R.color.button_text_color)
        val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_bank)
        drawable?.let {
            val wrappedDrawable = DrawableCompat.wrap(it).mutate()
            DrawableCompat.setTint(wrappedDrawable, iconColor)
            binding.imageViewWalletIcon.setImageDrawable(wrappedDrawable)
        }

        binding.textViewWalletName.text = getString(R.string.all_wallets)
    }

    private fun observeViewModels() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.updateTransactions(transactions)
            setupPieChart(transactions)
        }

        viewModel.wallet.observe(this) { updatedWallet ->
            setupWallet(updatedWallet)
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
        private const val EXTRA_FILTER_TYPE = "filter_type"
        private const val FILTER_INCOME = "income"
        private const val FILTER_EXPENSE = "expense"

        fun newIntent(context: Context, wallet: Wallet): Intent {
            val intent = Intent(context, TransactionListActivity::class.java)
            intent.putExtra(EXTRA_WALLET, wallet)
            return intent
        }

        fun newIntentAllWallets(context: Context): Intent {
            return Intent(context, TransactionListActivity::class.java)
        }

        fun newIntentIncomes(context: Context, wallet: Wallet): Intent {
            val intent = newIntent(context, wallet)
            intent.putExtra(EXTRA_FILTER_TYPE, FILTER_INCOME)
            return intent
        }

        fun newIntentExpenses(context: Context, wallet: Wallet): Intent {
            val intent = newIntent(context, wallet)
            intent.putExtra(EXTRA_FILTER_TYPE, FILTER_EXPENSE)
            return intent
        }

        fun newIntentAllIncomes(context: Context): Intent {
            val intent = newIntentAllWallets(context)
            intent.putExtra(EXTRA_FILTER_TYPE, FILTER_INCOME)
            return intent
        }

        fun newIntentAllExpenses(context: Context): Intent {
            val intent = newIntentAllWallets(context)
            intent.putExtra(EXTRA_FILTER_TYPE, FILTER_EXPENSE)
            return intent
        }
    }
}
