package com.example.moneyflow.data

import com.example.moneyflow.R

object DefaultCategories {

    val defaultExpenseCategories = listOf(
        Category(name = "Здоровье", iconResId = R.drawable.ic_health, isIncome = false),
        Category(name = "Развлечения", iconResId = R.drawable.ic_leisure, isIncome = false),
        Category(name = "Дом", iconResId = R.drawable.ic_home, isIncome = false),
        Category(name = "Кафе", iconResId = R.drawable.ic_cafe, isIncome = false),
        Category(name = "Образование", iconResId = R.drawable.ic_education, isIncome = false),
        Category(name = "Подарки", iconResId = R.drawable.ic_gift, isIncome = false),
        Category(name = "Продукты", iconResId = R.drawable.ic_grocery, isIncome = false)
    )

    val defaultIncomeCategories = listOf(
        Category(name = "Test1", iconResId = R.drawable.ic_plane, isIncome = true),
        Category(name = "Test2", iconResId = R.drawable.ic_plane, isIncome = true),
        Category(name = "Test3", iconResId = R.drawable.ic_plane, isIncome = true),
        Category(name = "Test4", iconResId = R.drawable.ic_plane, isIncome = true),
        Category(name = "Test5", iconResId = R.drawable.ic_plane, isIncome = true),
        Category(name = "Test6", iconResId = R.drawable.ic_plane, isIncome = true),
        Category(name = "Test7", iconResId = R.drawable.ic_plane, isIncome = true),
    )
}