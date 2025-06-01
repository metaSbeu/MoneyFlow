package com.example.moneyflow.utils

import android.content.Context
import com.example.moneyflow.R
import com.example.moneyflow.data.Category

object DefaultCategories {

    fun getDefaultExpenseCategories(context: Context): List<Category> {
        return listOf(
            Category(name = context.getString(R.string.health), icon = "ic_health", isIncome = false),
            Category(name = context.getString(R.string.leisure), icon = "ic_leisure", isIncome = false),
            Category(name = context.getString(R.string.home), icon = "ic_home", isIncome = false),
            Category(name = context.getString(R.string.cafe), icon = "ic_cafe", isIncome = false),
            Category(name = context.getString(R.string.education), icon = "ic_education", isIncome = false),
            Category(name = context.getString(R.string.gift), icon = "ic_gift", isIncome = false),
            Category(name = context.getString(R.string.grocery), icon = "ic_grocery", isIncome = false),
            Category(name = context.getString(R.string.other), icon = "ic_questionmark", isIncome = false),
        )
    }

    fun getDefaultIncomeCategories(context: Context): List<Category> {
        return listOf(
            Category(name = context.getString(R.string.salary), icon = "ic_salary", isIncome = true),
            Category(name = context.getString(R.string.investments), icon = "ic_investments", isIncome = true),
            Category(name = context.getString(R.string.gift), icon = "ic_gift", isIncome = true),
            Category(name = context.getString(R.string.scholarship), icon = "ic_student", isIncome = true),
            Category(name = context.getString(R.string.cashback), icon = "ic_cashback", isIncome = true),
            Category(name = context.getString(R.string.other), icon = "ic_questionmark", isIncome = true)
        )
    }

    val defaultCategoryIcons = listOf(
        "ic_health", "ic_leisure", "ic_home", "ic_cafe", "ic_education", "ic_gift", "ic_grocery", "ic_questionmark",
        "ic_salary",
        "ic_student",
        "ic_cashback",
        "ic_movie", "ic_game", "ic_sport", "ic_food", "ic_pet", "ic_fruit", "ic_clothes", "ic_shoe",
        "ic_diamond", "ic_furniture", "ic_music", "ic_computer", "ic_bicycle", "ic_wifi", "ic_car",
        "ic_plane",
        "ic_tooth",
        "ic_train_bus"
    ).distinct().map { iconName ->
        Category(name = "", icon = iconName, isIncome = false)
    }
}