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
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.data.TransactionWithCategory
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.utils.getDrawableResId
import com.example.moneyflow.databinding.ActivityTransactionListBinding
import com.example.moneyflow.ui.adapters.TransactionAdapter
import com.example.moneyflow.ui.viewmodels.TransactionListViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.datepicker.MaterialDatePicker
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

    private fun setupPieChart(transactions: List<TransactionWithCategory>) {
        val pieChart = binding.pieChart

        // Базовые настройки
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.setDrawCenterText(true)
        pieChart.legend.isEnabled = false

        // Подготовка данных
        val entries = preparePieChartData(transactions)
        val labelTextColorMF = ContextCompat.getColor(this, R.color.text_color_primary)
        val valueTextColorMF = ContextCompat.getColor(this, R.color.text_color_primary)
        pieChart.setEntryLabelColor(labelTextColorMF)
        // Обработка пустых данных
        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "Нет данных"
            return
        }

        // Создание набора данных
        val dataSet = PieDataSet(entries, "Категории транзакций").apply {
            sliceSpace = 2f
            colors = getPieChartColors()
            valueTextSize = 12f
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1OffsetPercentage = 80f

            valueTextColor = valueTextColorMF
            valueLineColor = ContextCompat.getColor(this@TransactionListActivity, R.color.item_background) // Use context here
            valueLineWidth = 1f
        }

        // Применение данных
        val pieData = PieData(dataSet)
        pieChart.data = pieData

        // Анимация
        pieChart.animateY(1000, Easing.EaseInOutQuad)

        // Обновление отображения
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

        // Вычисляем сумму для каждой категории
        for (transaction in transactions) {
            val categoryName = transaction.category.name
            val amount = transaction.transaction.sum
            categorySums[categoryName] = categorySums.getOrDefault(categoryName, 0.0) + amount
        }

        // Преобразуем в формат PieEntry для пайчарта
        return categorySums.map { PieEntry(it.value.toFloat(), it.key) }
    }

    private fun resetCardViewBackgroundColor() {
        // Получаем цвет из текущей темы
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.cardBackgroundColor, typedValue, true)
        val backgroundColor = typedValue.data

        // Устанавливаем цвет
        binding.cardViewExpenses.setCardBackgroundColor(backgroundColor)
        binding.cardViewIncomes.setCardBackgroundColor(backgroundColor)
    }
    private fun changeBackgroundColor(cardView: CardView) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.my_light_primary))
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
        val iconResId = baseContext.getDrawableResId(wallet.icon)
        binding.imageViewWalletIcon.setImageResource(iconResId)
        val formatted = wallet.balance.formatWithSpaces()
        binding.textViewWalletName.text = getString(R.string.wallet_main_info, wallet.name, formatted)
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


    override fun onResume() {
        super.onResume()
        viewModel.loadTransactions(wallet?.id)
    }

    private fun observeViewModels() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.transactions = transactions
            setupPieChart(transactions) // Обновляем пайчарт
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