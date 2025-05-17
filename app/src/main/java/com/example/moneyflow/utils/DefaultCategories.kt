package com.example.moneyflow.utils

import com.example.moneyflow.data.Category

object DefaultCategories {

    val defaultExpenseCategories = listOf(
        Category(name = "Здоровье", icon = "ic_health", isIncome = false),
        Category(name = "Развлечения", icon = "ic_leisure", isIncome = false),
        Category(name = "Дом", icon = "ic_home", isIncome = false),
        Category(name = "Кафе", icon = "ic_cafe", isIncome = false),
        Category(name = "Образование", icon = "ic_education", isIncome = false),
        Category(name = "Подарки", icon = "ic_gift", isIncome = false),
        Category(name = "Продукты", icon = "ic_grocery", isIncome = false),
        Category(name = "Другое", icon = "ic_questionmark", isIncome = false),
    )

    val defaultIncomeCategories = listOf(
        Category(name = "Зарплата", icon = "ic_salary", isIncome = true),
        Category(name = "Инвестиции", icon = "ic_investments", isIncome = true),
        Category(name = "Подарки", icon = "ic_gift", isIncome = true),
        Category(name = "Стипендия", icon = "ic_student", isIncome = true),
        Category(name = "Кэшбэк", icon = "ic_cashback", isIncome = true),
        Category(name = "Другое", icon = "ic_questionmark", isIncome = true)
    )

    val defaultCategoryIcons = listOf(
        Category(name = "", icon = "ic_movie", isIncome = false),
        Category(name = "", icon = "ic_game", isIncome = false),
        Category(name = "", icon = "ic_sport", isIncome = false),
        Category(name = "", icon = "ic_food", isIncome = false),
        Category(name = "", icon = "ic_pet", isIncome = false),
        Category(name = "", icon = "ic_fruit", isIncome = false),
        Category(name = "", icon = "ic_clothes", isIncome = false),
        Category(name = "", icon = "ic_shoe", isIncome = false),
        Category(name = "", icon = "ic_diamond", isIncome = false),
        Category(name = "", icon = "ic_furniture", isIncome = false),
        Category(name = "", icon = "ic_music", isIncome = false),
        Category(name = "", icon = "ic_computer", isIncome = false),
        Category(name = "", icon = "ic_bicycle", isIncome = false),
        Category(name = "", icon = "ic_wifi", isIncome = false),
        Category(name = "", icon = "ic_car", isIncome = false),
        Category(name = "", icon = "ic_plane", isIncome = false),
        Category(name = "", icon = "ic_tooth", isIncome = false),
        Category(name = "", icon = "ic_finance", isIncome = false),
        Category(name = "", icon = "ic_family", isIncome = false),
        Category(name = "", icon = "ic_train_bus", isIncome = false),
    )
}