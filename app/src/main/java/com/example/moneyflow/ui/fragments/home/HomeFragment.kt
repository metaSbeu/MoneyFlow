package com.example.moneyflow.ui.fragments.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewmodel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        viewmodel = ViewModelProvider(this)[HomeViewModel::class.java]
        adapter = WalletAdapter(
            {},
            {
                startActivity(AddWalletActivity.newIntent(requireContext()))
            })

        viewmodel.wallets.observe(viewLifecycleOwner) {
            adapter.wallets = it
        }

        binding.recyclerViewWallets.adapter = adapter

        clickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewmodel.refreshList()
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

}
