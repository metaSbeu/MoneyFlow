package com.example.moneyflow.utils

import com.example.moneyflow.data.Wallet

object DefaultWallets {

    val defaultWallets = listOf(
        Wallet(
            id = 0,
            name = "Наличные",
            balance = 0.0,
            icon = "ic_cash"
        )
    )
}