package com.example.moneyflow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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
            val intent = TransactionAddActivity.newIntent(requireContext())
            startActivity(intent)
        }

        binding.cardViewBalance.setOnClickListener {
            val intent = TransactionListActivity.newIntent(requireContext())
            startActivity(intent)
        }
    }

    private fun testWallets(): List<Wallet> {
        val wallets = mutableListOf<Wallet>()
        repeat(1000) {
            val wallet = Wallet("Wallet ${it + 1}: ", it * 1000)
            wallets.add(wallet)
        }
        return wallets
    }
}
