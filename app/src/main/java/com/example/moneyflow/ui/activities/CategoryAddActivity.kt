package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private var selectedCategory: Category? = null
    private var isIncome: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isIncome = intent.getBooleanExtra(EXTRA_IS_INCOME_CATEGORY, false)

        adapter = CategoryAdapter(
            {
                selectedCategory = it
            }, {}, showAddButton = false, isIncome = isIncome!!
        )

        viewModel = ViewModelProvider(this)[CategoryAddViewModel::class.java]

        viewModel.defaultIcons.observe(this) { defaultIcons ->
            adapter.categories = defaultIcons
        }

        binding.recyclerViewCategories.adapter = adapter

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()

            if (name.isBlank()) {
                Toast.makeText(this, getString(R.string.type_category_name), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val selected = selectedCategory
            if (selected == null) {
                Toast.makeText(this, getString(R.string.choose_category_icon), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val category = Category(name = name, icon = selected.icon, isIncome = isIncome == true)
            viewModel.addCategory(category)

        }
        viewModel.shouldCloseScreen.observe(this) { shouldCloseScreen ->
            if (shouldCloseScreen) {
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_IS_INCOME_CATEGORY = "isIncome"

        fun newIntent(context: Context, isIncome: Boolean): Intent {
            val intent = Intent(context, CategoryAddActivity::class.java)
            intent.putExtra(EXTRA_IS_INCOME_CATEGORY, isIncome)
            return intent
        }
    }
}