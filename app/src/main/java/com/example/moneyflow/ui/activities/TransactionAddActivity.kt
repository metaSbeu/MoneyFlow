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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.databinding.ActivityTransactionAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel
import com.example.moneyflow.utils.Formatter.convertToRub
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.utils.IconResolver
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
        setupInsets()
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
            val sumText = binding.editTextSum.text.toString()
            if (sumText.isBlank()) {
                Toast.makeText(this, getString(R.string.enter_sum), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sum = sumText.toDoubleOrNull()
            if (sum == null || sum <= 0) {
                Toast.makeText(
                    this, getString(R.string.sum_must_be_more_than_zero), Toast.LENGTH_SHORT
                ).show()
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

        binding.buttonFinish.setOnClickListener {
            finish()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSumEditTextValidation() {
        binding.editTextSum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

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

    fun setupAdapters() {
        categoriesAdapter = CategoryAdapter(onItemClick = {
            selectedCategory = it
        }, onAddClick = {
            startActivity(CategoryAddActivity.newIntent(this, isIncomeSelected))
        }, showAddButton = true, isIncome = false, onFirstCategorySelected = {
            selectedCategory = it
        })
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
        }

        viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
            if (shouldCloseScreen) {
                finish()
            }
        }

        viewModel.wallet.observe(this) { wallet ->
            val formatted = wallet.balance.formatWithSpaces(this)
            val iconResId = IconResolver.resolve(wallet.icon)
//            binding.imageViewWalletIcon.setImageResource(iconResId)
//            ImageViewCompat.setImageTintList(
//                binding.imageViewWalletIcon,
//                ContextCompat.getColorStateList(this, R.color.text_color_primary)
//            )
            binding.textViewWalletNameAndBalance.text = getString(
                R.string.wallet_name_wallet_balance, wallet.name, formatted
            )
        }
    }

    fun datePicker() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.select_date))
                .build()
        datePicker.show(supportFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            selectedDateInMillis = selection

            val locale = resources.configuration.locales[0]
            val dateFormat = SimpleDateFormat("d MMM", locale)
            val formattedDate = getString(R.string.date, dateFormat.format(calendar.time))

            binding.textViewDate.text = formattedDate
        }
    }

    fun setTodayDate() {
        val today = java.util.Calendar.getInstance()
        val locale = resources.configuration.locales[0]
        val dateFormat = SimpleDateFormat("d MMM", locale)
        val formattedDate = getString(R.string.date, dateFormat.format(today.time))
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