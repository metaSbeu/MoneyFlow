package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.databinding.ActivityCategoryAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.CategoryAddViewModel

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var viewModel: CategoryAddViewModel
    private lateinit var selectedCategory: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)


        adapter = CategoryAdapter(
            {
                selectedCategory = it
            },
            {}, showAddButton = false
        )

        viewModel = ViewModelProvider(this)[CategoryAddViewModel::class.java]

        viewModel.defaultIcons.observe(this) {
            adapter.categories = viewModel.getUpDefaultCategoryIcons()
        }

        binding.recyclerViewCategories.adapter = adapter

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()

            if (name.isBlank()) {
                Toast.makeText(this, "Введите название категории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val icon = selectedCategory?.iconResId
            if (icon == null) {
                Toast.makeText(this, "Выберите иконку категории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = Category(name = name, iconResId = icon, isIncome = false)

            viewModel.addCategory(category)

        }
        viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
            if (shouldCloseScreen) {
                finish()
            }
        }
        setupInsets()
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
            return Intent(context, CategoryAddActivity::class.java)
        }
    }
}