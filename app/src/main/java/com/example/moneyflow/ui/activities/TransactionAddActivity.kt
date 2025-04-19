package com.example.moneyflow.ui.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.databinding.ActivityTransactionAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.adapters.TransactionAdapter
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionAddBinding
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        toggleGroup()
        setTodayDate()

        adapter = CategoryAdapter {}
        binding.recyclerViewCategories.adapter = adapter
        adapter.categories = generateCategories()

        binding.buttonDate.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()
            datePicker.show(supportFragmentManager, "tag")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selection

                val locale = resources.configuration.locales[0]
                val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
                val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"

                binding.textViewDate.text = formattedDate
            }
        }
    }

    fun setTodayDate() {
        val today = java.util.Calendar.getInstance()
        val locale = resources.configuration.locales[0]
        val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
        val formattedDate = "Дата: ${dateFormat.format(today.time)}"
        binding.textViewDate.text = formattedDate
    }

    private fun generateCategories(): MutableList<Category> {
        val categories = mutableListOf<Category>()
        repeat(160) {
            categories.add(Category("Category $it"))
        }
        return categories
    }

    fun toggleGroup() {
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        toggleGroup.check(R.id.buttonExpense)
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