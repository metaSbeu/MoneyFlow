package com.example.moneyflow.ui.fragments.home

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.moneyflow.R
import com.example.moneyflow.data.SwipeCallback
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.FragmentHomeBinding
import com.example.moneyflow.ui.activities.WalletAddActivity
import com.example.moneyflow.ui.activities.TransactionAddActivity
import com.example.moneyflow.ui.activities.TransactionListActivity
import com.example.moneyflow.ui.activities.WalletEditActivity
import com.example.moneyflow.ui.adapters.WalletAdapter
import java.time.LocalDate
import java.time.Month

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: WalletAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewmodel: HomeViewModel
    private var selectedWallet: Wallet? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        viewmodel = ViewModelProvider(this)[HomeViewModel::class.java]
        adapter = WalletAdapter(
            { wallet ->
                selectedWallet = wallet
            },
            {
                startActivity(WalletAddActivity.newIntent(requireContext()))
            }
        )
        setupItemTouchHelper()
        viewmodel.wallets.observe(viewLifecycleOwner) {
            adapter.wallets = it
        }
        viewmodel.overallBalance.observe(viewLifecycleOwner) {
            val rounded = String.format("%.2f", it)
            binding.textViewBalance.text = getString(R.string.balance, rounded)
        }
        binding.recyclerViewWallets.adapter = adapter
        binding.textViewCurrentMonthExpenses.text = getString(R.string.current_month, getCurrentMonth())
        binding.textViewCurrentMonthIncomes.text = getString(R.string.current_month, getCurrentMonth())
        setupClickListeners()
    }

    fun setupItemTouchHelper() {
        val itemTouchHelper = ItemTouchHelper(SwipeCallback(
            { position ->
                viewmodel.deleteWallet(adapter.wallets[position])
            },
            { position ->
                val wallet = adapter.wallets[position]
                startActivity(WalletEditActivity.newIntent(requireContext(), wallet))
            }
        ))

        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWallets)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentMonth(): String {
        val month = LocalDate.now().month
        return when (month) {
            Month.JANUARY -> "январе"
            Month.FEBRUARY -> "феврале"
            Month.MARCH -> "марте"
            Month.APRIL -> "апреле"
            Month.MAY -> "мае"
            Month.JUNE -> "июне"
            Month.JULY -> "июле"
            Month.AUGUST -> "августе"
            Month.SEPTEMBER -> "сентябре"
            Month.OCTOBER -> "октябре"
            Month.NOVEMBER -> "ноябре"
            Month.DECEMBER -> "декабре"
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.refreshWalletsList()
    }

    fun setupClickListeners() {
        binding.cardViewAddTransaction.setOnClickListener {
            if (selectedWallet == null) {
                Toast.makeText(requireContext(), "Сначала выберите счет", Toast.LENGTH_SHORT).show()
            } else {
                val intent = TransactionAddActivity.newIntent(requireContext(), selectedWallet!!.id)
                startActivity(intent)
            }
        }

        binding.cardViewBalance.setOnClickListener {
            val intent = TransactionListActivity.Companion.newIntent(requireContext())
            startActivity(intent)
        }
    }
}
