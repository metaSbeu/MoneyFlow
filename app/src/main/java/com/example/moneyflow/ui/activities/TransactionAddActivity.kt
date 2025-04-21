package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.databinding.ActivityTransactionAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat

class TransactionAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionAddBinding
    private lateinit var adapter: CategoryAdapter

    private lateinit var database: MainDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionAddBinding.inflate(layoutInflater)
        database = MainDatabase.getDb(application)

        setContentView(binding.root)
        setupInsets()
        setupToggleGroup()
        setTodayDate()

        val testCategories = mutableListOf<Category>()

        adapter = CategoryAdapter(
            {

            },
            {
                startActivity(AddCategoryActivity.newIntent(this))
            })

        binding.recyclerViewCategories.adapter = adapter
        adapter.categories = database.categoryDao().getCategories()

        binding.buttonDate.setOnClickListener {
            datePicker()
        }

        binding.buttonSave.setOnClickListener {

        }
        initializeDefaultCategories()
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

            val locale = resources.configuration.locales[0]
            val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
            val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"

            binding.textViewDate.text = formattedDate
        }
    }

    fun initializeDefaultCategories() {
        val defaultCategories = listOf(
            Category(name = "Здоровье", icon = "ic_health"),
            Category(name = "Досуг", icon = "ic_leisure"),
            Category(name = "Дом", icon = "ic_home"),
            Category(name = "Кафе", icon = "ic_cafe"),
            Category(name = "Образование", icon = "ic_education"),
            Category(name = "Подарки", icon = "ic_gift"),
            Category(name = "Продукты", icon = "ic_grocery")
        )
        val count = database.categoryDao().getCount()
        if (count == 0) {
            database.categoryDao().insertAll(defaultCategories)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.categories = database.categoryDao().getCategories()
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