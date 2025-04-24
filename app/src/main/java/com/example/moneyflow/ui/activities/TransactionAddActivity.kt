package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.databinding.ActivityTransactionAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat

class TransactionAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionAddBinding

    private lateinit var expenseAdapter: CategoryAdapter
    private lateinit var incomeAdapter: CategoryAdapter

    private lateinit var viewModel: TransactionAddViewModel

    private lateinit var selectedCategory: Category

    private var isIncomeSelected = false
    private var selectedDateInMillis = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[TransactionAddViewModel::class.java]
        setupInsets()
        setupToggleGroup()
        setTodayDate()

        val walletId = intent.getIntExtra(EXTRA_WALLET, 1)
        viewModel.getWalletById(walletId)

        setupAdapters()

        binding.recyclerViewExpenseCategories.adapter = expenseAdapter
        binding.recyclerViewIncomeCategories.adapter = incomeAdapter

        observeViewModel()

        binding.buttonDate.setOnClickListener {
            datePicker()
        }

        binding.buttonSave.setOnClickListener {
            val category = selectedCategory
            if (category == null) {
                Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sumText = binding.editTextSum.text.toString()
            if (sumText.isBlank()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = 0,
                categoryId = selectedCategory.id,
                walletId = walletId,
                sum = sumText.toDouble(),
                isIncome = isIncomeSelected,
                note = binding.editTextComment.text.toString(),
                createdAt = selectedDateInMillis
            )
            viewModel.saveTransaction(transaction)
        }

        binding.textViewEditCategory.setOnClickListener {
            val intent = CategoryEditActivity.newIntent(this, selectedCategory)
            startActivity(intent)
        }


    }

    fun setupAdapters() {
        expenseAdapter = CategoryAdapter(
            onItemClick = {
                selectedCategory = it
            },
            onAddClick = {
                startActivity(CategoryAddActivity.newIntent(this, false))
            },
            showAddButton = true,
            isIncome = false,
            onFirstCategorySelected = {
                selectedCategory = it
            }
        )

        incomeAdapter = CategoryAdapter(
            onItemClick = {
                selectedCategory = it
            },
            onAddClick = {
                startActivity(CategoryAddActivity.newIntent(this, true))
            },
            showAddButton = true,
            isIncome = true,
            onFirstCategorySelected = {
                selectedCategory = it
            }
        )
    }

    fun switchRecyclerViewVisibility(isIncomeSelected: Boolean) {
        if (isIncomeSelected) {
            binding.recyclerViewIncomeCategories.visibility = View.VISIBLE
            binding.recyclerViewExpenseCategories.visibility = View.GONE
        } else {
            binding.recyclerViewIncomeCategories.visibility = View.GONE
            binding.recyclerViewExpenseCategories.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCategories()
    }

    fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            val expenseCategories = categories.filter { !it.isIncome }
            val incomeCategories = categories.filter { it.isIncome }

            expenseAdapter.categories = expenseCategories
            incomeAdapter.categories = incomeCategories

            // Установка категории по умолчанию
            val defaultCategory = if (isIncomeSelected) incomeCategories.firstOrNull() else expenseCategories.firstOrNull()
            if (defaultCategory != null) {
                selectedCategory = defaultCategory
                // Можно вызвать notifyDataSetChanged(), если хочешь выделить её визуально
            }
        }

        viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
            if (shouldCloseScreen) {
                finish()
            }
        }

        viewModel.wallet.observe(this) { wallet ->
            val roundedBalance = String.format("%.2f", wallet.balance)
            binding.imageViewWalletIcon.setBackgroundResource(wallet.iconResId)
            binding.textViewWalletNameAndBalance.text = getString(
                R.string.wallet_name_wallet_balance,
                wallet.name,
                roundedBalance
            )
        }
    }

    fun datePicker() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()
        datePicker.show(supportFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            selectedDateInMillis = selection

            val locale = resources.configuration.locales[0]
            val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
            val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"

            binding.textViewDate.text = formattedDate
        }
    }

    fun setTodayDate() {
        val today = java.util.Calendar.getInstance()
        val locale = resources.configuration.locales[0]
        val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
        val formattedDate = "Дата: ${dateFormat.format(today.time)}"
        binding.textViewDate.text = formattedDate
    }

    fun setupToggleGroup() {
        val toggleGroup = binding.toggleGroup
        toggleGroup.check(R.id.buttonExpense)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isIncomeSelected = checkedId == R.id.buttonIncome
                switchRecyclerViewVisibility(isIncomeSelected)
            }
        }
        switchRecyclerViewVisibility(isIncomeSelected)
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

        fun newIntent(context: Context, walletId: Int): Intent {
            val intent = Intent(context, TransactionAddActivity::class.java)
            intent.putExtra(EXTRA_WALLET, walletId)
            return intent
        }
    }
}