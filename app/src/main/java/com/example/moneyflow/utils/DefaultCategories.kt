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
        Category(name = "Продукты", icon = "ic_grocery", isIncome = false)
    )

    val defaultIncomeCategories = listOf(
        Category(name = "Зарплата", icon = "ic_plane", isIncome = true),
        Category(name = "Фриланс", icon = "ic_plane", isIncome = true),
        Category(name = "Инвестиции", icon = "ic_plane", isIncome = true),
        Category(name = "Подарки", icon = "ic_plane", isIncome = true),
        Category(name = "Стипендия", icon = "ic_plane", isIncome = true),
        Category(name = "Кэшбэк", icon = "ic_plane", isIncome = true),
        Category(name = "Другое", icon = "ic_plane", isIncome = true)
    )
}