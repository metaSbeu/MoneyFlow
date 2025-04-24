package com.example.moneyflow.data

import com.example.moneyflow.R

object DefaultWallets {

    val defaultWallets = listOf(
        Wallet(
            id = 0,
            name = "Наличные",
            balance = 0.0,
            iconResId = R.drawable.ic_cash,
            currency = "RUB"
        )
    )
}