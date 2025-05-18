package com.example.moneyflow.utils

import android.content.Context
import com.example.moneyflow.R
import com.example.moneyflow.data.Wallet

object DefaultWallets {

    fun getDefaultWallets(context: Context): List<Wallet> {
        return listOf(
            Wallet(
                id = 0,
                name = context.getString(R.string.cash),
                balance = 0.0,
                icon = "ic_cash"
            )
        )
    }
}
