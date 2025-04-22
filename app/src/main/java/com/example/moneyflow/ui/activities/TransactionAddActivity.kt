package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.databinding.ActivityTransactionAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.TransactionAddViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import kotlin.random.Random

class TransactionAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionAddBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var viewModel: TransactionAddViewModel
    private var selectedCategory: Category? = null
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

        adapter = CategoryAdapter(
            {
                selectedCategory = it
            },
            {
                startActivity(CategoryAddActivity.newIntent(this))
            })

        binding.recyclerViewCategories.adapter = adapter

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
                categoryId = selectedCategory!!.id,
                walletId = 1,
                sum = sumText.toDouble(),
                isIncome = isIncomeSelected,
                note = binding.editTextComment.text.toString(),
                createdAt = selectedDateInMillis
            )
            viewModel.saveTransaction(transaction)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCategories()
    }

    fun observeViewModel() {
        viewModel.categories.observe(this) {
            adapter.categories = it
        }

        viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
            if (shouldCloseScreen) {
                finish()
            }
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
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        toggleGroup.check(R.id.buttonExpense)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isIncomeSelected = checkedId == R.id.buttonIncome
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
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, TransactionAddActivity::class.java)
            return intent
        }
    }
}