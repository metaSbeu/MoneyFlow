package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
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
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.databinding.ActivityTransactionAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel
import com.example.moneyflow.utils.Formatter.convertToRub
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.utils.getDrawableResId
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat

class TransactionAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionAddBinding
    private lateinit var categoriesAdapter: CategoryAdapter
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

        setupToggleGroup()
        setTodayDate()

        val walletId = intent.getIntExtra(EXTRA_WALLET, 1)
        viewModel.getWalletById(walletId)

        setupAdapters()
        binding.recyclerViewCategories.adapter = categoriesAdapter

        observeViewModel()

        setupSumEditTextValidation()

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

            val sum = sumText.toDoubleOrNull()
            if (sum == null || sum <= 0) {
                Toast.makeText(this, "Введите корректную сумму больше 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sumInRub = convertToRub(this, sum)

            val transaction = Transaction(
                id = 0,
                categoryId = selectedCategory.id,
                walletId = walletId,
                sum = sumInRub,
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

    private fun setupSumEditTextValidation() {
        binding.editTextSum.addTextChangedListener(object : TextWatcher {
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
                        editable.delete(0, 1) // Удаляем лидирующий 0, если он не является началом десятичной части
                    }
                    if (text == ".") {
                        editable.clear() // Запрещаем ввод только точки
                    }
                }
            }
        })
    }

    fun setupAdapters() {
        categoriesAdapter = CategoryAdapter(
            onItemClick = {
                selectedCategory = it
            },
            onAddClick = {
                startActivity(CategoryAddActivity.newIntent(this, isIncomeSelected))
            },
            showAddButton = true,
            isIncome = false,
            onFirstCategorySelected = {
                selectedCategory = it
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCategories()
    }

    fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            val expenseCategories = categories.filter { !it.isIncome }
            val incomeCategories = categories.filter { it.isIncome }

            if (isIncomeSelected) {
                categoriesAdapter.categories = incomeCategories
            } else {
                categoriesAdapter.categories = expenseCategories
            }

            val defaultCategory =
                if (isIncomeSelected) incomeCategories.firstOrNull() else expenseCategories.firstOrNull()
            if (defaultCategory != null) {
                selectedCategory = defaultCategory
            }
        }

        viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
            if (shouldCloseScreen) {
                finish()
            }
        }

        viewModel.wallet.observe(this) { wallet ->
            val formatted = wallet.balance.formatWithSpaces(this)
            val iconResId = baseContext.getDrawableResId(wallet.icon)
            binding.imageViewWalletIcon.setImageResource(iconResId)
            binding.textViewWalletNameAndBalance.text = getString(
                R.string.wallet_name_wallet_balance,
                wallet.name,
                formatted
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
            val dateFormat = SimpleDateFormat("d MMM", locale)
            val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"

            binding.textViewDate.text = formattedDate
        }
    }

    fun setTodayDate() {
        val today = java.util.Calendar.getInstance()
        val locale = resources.configuration.locales[0]
        val dateFormat = SimpleDateFormat("d MMM", locale)
        val formattedDate = "Дата: ${dateFormat.format(today.time)}"
        binding.textViewDate.text = formattedDate
    }

    fun setupToggleGroup() {
        val toggleGroup = binding.toggleGroup
        toggleGroup.check(R.id.buttonExpense)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isIncomeSelected = checkedId == R.id.buttonIncome
                viewModel.categories.value?.let { categories ->
                    val filteredCategories = categories.filter { it.isIncome == isIncomeSelected }
                    categoriesAdapter.categories = filteredCategories
                    categoriesAdapter.isIncome = isIncomeSelected
                }
            }
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