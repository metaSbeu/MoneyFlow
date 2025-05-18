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