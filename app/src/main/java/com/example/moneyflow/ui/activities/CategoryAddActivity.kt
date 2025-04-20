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
        adapter = CategoryAdapter({
            selectedCategory = it
        }, {}, showAddButton = false)

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
            val icon = selectedCategory?.icon
            if (icon == null) {
                Toast.makeText(this, "Выберите иконку категории", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = Category(name = name, icon = icon)

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
            Category(name = "", icon = "ic_movie"),
            Category(name = "", icon = "ic_game"),
            Category(name = "", icon = "ic_sport"),
            Category(name = "", icon = "ic_food"),
            Category(name = "", icon = "ic_pet"),
            Category(name = "", icon = "ic_fruit"),
            Category(name = "", icon = "ic_clothes"),
            Category(name = "", icon = "ic_shoe"),
            Category(name = "", icon = "ic_diamond"),
            Category(name = "", icon = "ic_furniture"),
            Category(name = "", icon = "ic_music"),
            Category(name = "", icon = "ic_computer"),
            Category(name = "", icon = "ic_bicycle"),
            Category(name = "", icon = "ic_wifi"),
            Category(name = "", icon = "ic_car"),
            Category(name = "", icon = "ic_plane"),
            Category(name = "", icon = "ic_tooth"),
            Category(name = "", icon = "ic_finance"),
            Category(name = "", icon = "ic_family"),
            Category(name = "", icon = "ic_train_bus"),
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