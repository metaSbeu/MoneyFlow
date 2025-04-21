package com.example.moneyflow.ui.fragments.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.moneyflow.R
import com.example.moneyflow.data.MainDatabase
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.FragmentHomeBinding
import com.example.moneyflow.ui.activities.AddWalletActivity
import com.example.moneyflow.ui.activities.TransactionAddActivity
import com.example.moneyflow.ui.activities.TransactionListActivity
import com.example.moneyflow.ui.adapters.WalletAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: WalletAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: MainDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        database = MainDatabase.getDb(requireActivity().application)

        adapter = WalletAdapter(
            {},
            {
                startActivity(AddWalletActivity.newIntent(requireContext()))
            })
        adapter.wallets = database.walletDao().getWallets()
        binding.recyclerViewWallets.adapter = adapter

        clickListeners()
    }

    override fun onResume() {
        super.onResume()
        adapter.wallets = database.walletDao().getWallets()
    }

    fun clickListeners() {
        binding.cardViewAddTransaction.setOnClickListener {
            val intent = TransactionAddActivity.Companion.newIntent(requireContext())
            startActivity(intent)
        }

        binding.cardViewBalance.setOnClickListener {
            val intent = TransactionListActivity.Companion.newIntent(requireContext())
            startActivity(intent)
        }
    }

//    private fun testWallets(): List<Wallet> {
//        val wallets = mutableListOf<Wallet>()
//        repeat(10) {
//            val wallet = Wallet(it, "Wallet ${it + 1}: ", it * 1000)
//            wallets.add(wallet)
//        }
//        return wallets
//    }
}
