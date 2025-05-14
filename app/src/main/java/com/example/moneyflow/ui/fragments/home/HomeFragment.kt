package com.example.moneyflow.ui.fragments.home

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.moneyflow.R
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.databinding.FragmentHomeBinding
import com.example.moneyflow.ui.activities.TransactionAddActivity
import com.example.moneyflow.ui.activities.TransactionListActivity
import com.example.moneyflow.ui.activities.WalletAddActivity
import com.example.moneyflow.ui.activities.WalletEditActivity
import com.example.moneyflow.ui.adapters.WalletAdapter
import com.example.moneyflow.utils.Formatter
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.utils.SwipeCallback
import java.time.LocalDate
import java.time.Month

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: WalletAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewmodel: HomeViewModel

    private var selectedWallet: Wallet? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        viewmodel = ViewModelProvider(this)[HomeViewModel::class.java]
        adapter = WalletAdapter(
            { wallet ->
                adapter.deselectAll()
                adapter.selectWallet(wallet)
                selectedWallet = wallet
            },
            {
                startActivity(WalletAddActivity.newIntent(requireContext()))
            }
        )
        adapter.selectAll() // По умолчанию выделяем "Все счета"
        viewmodel.wallets.observe(viewLifecycleOwner) { wallets ->
            adapter.wallets = wallets
            // Автоматически выбираем единственный кошелек, если он есть
            if (wallets.size == 1 && selectedWallet == null) {
                val singleWallet = wallets.first()
                adapter.deselectAll()
                adapter.selectWallet(singleWallet)
                selectedWallet = singleWallet
            }
        }

        viewmodel.overallBalance.observe(viewLifecycleOwner) { balance ->
            val formatted = balance.formatWithSpaces(requireContext())
            binding.textViewBalance.text = getString(R.string.balance, formatted)
        }

        binding.recyclerViewWallets.adapter = adapter

        binding.textViewChooseAll.setOnClickListener {
            adapter.selectAll()
            selectedWallet = null
        }

        setupMonthData()
        setupItemTouchHelper()
        setupClickListeners()

        binding.recyclerViewWallets.postDelayed({
            val viewHolder = binding.recyclerViewWallets.findViewHolderForAdapterPosition(0)
            viewHolder?.itemView?.let { view ->
                // Анимация свайпа влево и назад
                view.animate()
                    .translationX(-100f)
                    .setDuration(200)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
            }
        }, 800) // Подождать, чтобы элементы успели отрисоваться

    }

    private fun setupMonthData() {
        binding.textViewCurrentMonthExpenses.text =
            getString(R.string.current_month, getCurrentMonth())
        binding.textViewCurrentMonthIncomes.text =
            getString(R.string.current_month, getCurrentMonth())

        viewmodel.monthExpenses.observe(viewLifecycleOwner) { expenses ->
            val formatted = expenses.formatWithSpaces(requireContext())
            binding.textViewExpenses.text = formatted
        }

        viewmodel.monthIncomes.observe(viewLifecycleOwner) { incomes ->
            val formatted = incomes.formatWithSpaces(requireContext())
            binding.textViewIncomes.text = formatted
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить счет")
            .setMessage("Вы уверены, что хотите удалить счет '${adapter.wallets[position].name}'? Также удалятся все транзакции этого счета!")
            .setPositiveButton("Удалить") { _, _ ->
                // Удаляем счет
                viewmodel.deleteWallet(adapter.wallets[position])
                selectedWallet = null
                adapter.selectAll()
            }
            .setNegativeButton("Отмена") { _, _ ->
                // Возвращаем элемент на место
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                // На случай если пользователь закрыл диалог (например, тапнул вне окна)
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelper = ItemTouchHelper(
            SwipeCallback(
                adapter,
                { position ->
                    val wallet = adapter.wallets[position]
                    startActivity(WalletEditActivity.newIntent(requireContext(), wallet))
                },
                { position ->
                    showDeleteConfirmationDialog(position)

                }
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWallets)
    }

    private fun getCurrentMonth(): String {
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
        viewmodel.refreshWalletsList() // Обновляем список кошельков

        // Если есть выбранный кошелек, запрашиваем его актуальные данные
        selectedWallet?.let {
            viewmodel.getWalletById(it.id).observe(viewLifecycleOwner) { updatedWallet ->
                updatedWallet?.let {
                    selectedWallet = it
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardViewAddTransaction.setOnClickListener {
            if (selectedWallet == null) {
                Toast.makeText(requireContext(), "Выберите один счет", Toast.LENGTH_SHORT).show()
            } else {
                val intent = TransactionAddActivity.newIntent(requireContext(), selectedWallet!!.id)
                startActivity(intent)
            }
        }

        binding.cardViewBalance.setOnClickListener {
            val intent = if (selectedWallet == null) {
                TransactionListActivity.newIntentAllWallets(requireContext())
            } else {
                TransactionListActivity.newIntent(requireContext(), selectedWallet!!)
            }
            startActivity(intent)
        }

        binding.cardViewIncomes.setOnClickListener {
            val intent = if (selectedWallet == null) {
                TransactionListActivity.newIntentAllIncomes(requireContext())
            } else {
                TransactionListActivity.newIntentIncomes(requireContext(), selectedWallet!!)
            }
            startActivity(intent)
        }

        binding.cardViewExpenses.setOnClickListener {
            val intent = if (selectedWallet == null) {
                TransactionListActivity.newIntentAllExpenses(requireContext())
            } else {
                TransactionListActivity.newIntentExpenses(requireContext(), selectedWallet!!)
            }
            startActivity(intent)
        }

    }
}