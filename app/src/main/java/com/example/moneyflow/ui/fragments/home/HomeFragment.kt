package com.example.moneyflow.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.moneyflow.R
import com.example.moneyflow.ui.activities.TransactionAddActivity
import com.example.moneyflow.ui.activities.TransactionListActivity
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.ui.adapters.WalletAdapter
import com.example.moneyflow.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: WalletAdapter
    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        adapter = WalletAdapter()
        adapter.wallets = testWallets()
        binding.recyclerViewWallets.adapter = adapter

        binding.cardViewAddTransaction.setOnClickListener {
            val intent = TransactionAddActivity.Companion.newIntent(requireContext())
            startActivity(intent)
        }

        binding.cardViewBalance.setOnClickListener {
            val intent = TransactionListActivity.Companion.newIntent(requireContext())
            startActivity(intent)
        }
    }

    private fun testWallets(): List<Wallet> {
        val wallets = mutableListOf<Wallet>()
        repeat(10) {
            val wallet = Wallet(it, "Wallet ${it + 1}: ", it * 1000)
            wallets.add(wallet)
        }
        return wallets
    }
}
