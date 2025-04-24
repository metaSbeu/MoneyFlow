package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.IconsManager
import com.example.moneyflow.databinding.ActivityCategoryEditBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.CategoryEditViewModel

class CategoryEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryEditBinding
    private lateinit var adapter: CategoryAdapter
    private lateinit var selectedCategory: Category
    private lateinit var viewModel: CategoryEditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoryEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        viewModel = ViewModelProvider(this)[CategoryEditViewModel::class.java]

        val categoryFromIntent = intent.getSerializableExtra(EXTRA_CATEGORY) as? Category
        if (categoryFromIntent != null) {
            selectedCategory = categoryFromIntent
            binding.editTextNewName.setText(categoryFromIntent.name)
        } else {
            finish()
            return
        }

        adapter = CategoryAdapter(
            {
                selectedCategory = selectedCategory.copy(iconResId = it.iconResId)
//                adapter.notifyDataSetChanged()
            },
            {},
            showAddButton = false,
            isIncome = false
        )

        adapter.categories = IconsManager.getAllCategoryExpenseIcons()

        binding.recyclerViewCategories.adapter = adapter

        binding.buttonSave.setOnClickListener {
            val newName = binding.editTextNewName.text.toString().trim()
            val newIcon = selectedCategory.iconResId
            val newCategory = Category(
                selectedCategory.id,
                newName,
                newIcon,
                selectedCategory.isIncome
            )
            viewModel.updateCategory(newCategory)
        }
        viewModel.shouldCloseScreen.observe(this) {
            if (it) {
                finish()
            }
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog(categoryFromIntent)
        }
    }

    private fun showDeleteConfirmationDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.removal))
            .setMessage(getString(R.string.category_delete_confirmation_alert))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                viewModel.deleteCategory(category)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }


    fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        const val EXTRA_CATEGORY = "category"

        fun newIntent(context: Context, category: Category): Intent {
            val intent = Intent(context, CategoryEditActivity::class.java)
            intent.putExtra(EXTRA_CATEGORY, category)
            return intent
        }
    }
}