package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.databinding.ActivityCategoryAddBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    private lateinit var database: MainDatabase
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = MainDatabase.getDb(application)

        var selectedCategory: Category? = null
        adapter = CategoryAdapter(
            {
                selectedCategory = it
            },
            {}, showAddButton = false
        )

        adapter.categories = icons()
        binding.recyclerViewCategories.adapter = adapter

        binding.buttonSave.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()

            // Проверка на пустое имя
            if (name.isBlank()) {
                Toast.makeText(this, "Введите название категории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверка на отсутствие выбранной иконки
            val icon = selectedCategory?.iconResId
            if (icon == null) {
                Toast.makeText(this, "Выберите иконку категории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = Category(name = name, iconResId = icon, isIncome = false)

            database.categoryDao().insert(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()

            finish()
        }
        setupInsets()
    }

    fun icons(): List<Category> {
        val categories = listOf<Category>(
            Category(name = "", iconResId = R.drawable.ic_movie, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_game, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_sport, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_food, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_pet, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_fruit, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_clothes, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_shoe, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_diamond, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_furniture, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_music, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_computer, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_bicycle, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_wifi, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_car, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_plane, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_tooth, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_finance, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_family, isIncome = false),
            Category(name = "", iconResId = R.drawable.ic_train_bus, isIncome = false),
        )
        return categories
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