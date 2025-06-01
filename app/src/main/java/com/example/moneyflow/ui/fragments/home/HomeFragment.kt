package com.example.moneyflow.ui.fragments.home

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

        // Initialize the adapter without the onFirstCategorySelected callback,
        // as we'll handle the initial selection in the fragment.
        adapter = WalletAdapter({ wallet ->
            // On item click, deselect all and select the clicked one
            adapter.deselectAll()
            adapter.selectWallet(wallet)
            selectedWallet = wallet
        }, {
            // On add button click
            startActivity(WalletAddActivity.newIntent(requireContext()))
        })

        viewmodel.wallets.observe(viewLifecycleOwner) { wallets ->
            adapter.wallets = wallets // Update adapter with new list

            // --- START OF MODIFICATION ---
            // Logic to select the first wallet if no wallet is currently selected
            if (selectedWallet == null && wallets.isNotEmpty()) {
                val firstWallet = wallets.first()
                adapter.deselectAll() // Ensure no previous selections are active
                adapter.selectWallet(firstWallet) // Select the first wallet
                selectedWallet = firstWallet // Update the fragment's tracking variable
            } else if (wallets.isEmpty()) {
                // If there are no wallets, ensure nothing is selected
                selectedWallet = null
                adapter.deselectAll()
            }
            // --- END OF MODIFICATION ---
        }

        viewmodel.overallBalance.observe(viewLifecycleOwner) { balance ->
            val formatted = balance.formatWithSpaces(requireContext())
            binding.textViewBalance.text = formatted
        }

        binding.recyclerViewWallets.adapter = adapter

        binding.textViewChooseAll.setOnClickListener {
            adapter.selectAll() // This should deselect any specific wallet
            selectedWallet = null // Explicitly set to null for "all wallets" mode
        }

        setupMonthData()
        setupItemTouchHelper()
        setupClickListeners()

        // Your existing animation logic
        binding.recyclerViewWallets.postDelayed({
            val viewHolder = binding.recyclerViewWallets.findViewHolderForAdapterPosition(0)
            viewHolder?.itemView?.let { view ->
                view.animate().translationX(-100f).setDuration(200).withEndAction {
                    view.animate().translationX(0f).setDuration(200).start()
                }.start()
            }
        }, 800)
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
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.remove_wallet))
            .setMessage(getString(R.string.confirm_delete_wallet, adapter.wallets[position].name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewmodel.deleteWallet(adapter.wallets[position])
                // After deletion, the selectedWallet might be the one that was deleted.
                // Reset to null and let the observe block re-select the first available.
                selectedWallet = null
                // The observe block for wallets will handle re-selection or showing "all"
                // depending on what happens to the wallet list after deletion.
            }.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                adapter.notifyItemChanged(position)
            }.setOnCancelListener {
                adapter.notifyItemChanged(position)
            }.show()
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelper = ItemTouchHelper(SwipeCallback(adapter, { position ->
            val wallet = adapter.wallets[position]
            startActivity(WalletEditActivity.newIntent(requireContext(), wallet))
        }, { position ->
            showDeleteConfirmationDialog(position)
        }))
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWallets)
    }

    private fun getCurrentMonth(): String {
        val month = LocalDate.now().month
        return when (month) {
            Month.JANUARY -> getString(R.string.january)
            Month.FEBRUARY -> getString(R.string.february)
            Month.MARCH -> getString(R.string.march)
            Month.APRIL -> getString(R.string.april)
            Month.MAY -> getString(R.string.june) // Corrected from JUNE to MAY
            Month.JUNE -> getString(R.string.june)
            Month.JULY -> getString(R.string.july)
            Month.AUGUST -> getString(R.string.august)
            Month.SEPTEMBER -> getString(R.string.september)
            Month.OCTOBER -> getString(R.string.october)
            Month.NOVEMBER -> getString(R.string.november)
            Month.DECEMBER -> getString(R.string.december)
            // Add a default case for robustness
            else -> "" // Or throw an exception, depending on desired behavior
        }
    }


    override fun onResume() {
        super.onResume()
        viewmodel.refreshWalletsList()

        // It's crucial here to re-select the wallet if it's not null,
        // in case the adapter state was reset or the wallet itself changed.
        selectedWallet?.let { currentSelected ->
            viewmodel.getWalletById(currentSelected.id)
                .observe(viewLifecycleOwner) { updatedWallet ->
                    if (updatedWallet != null && updatedWallet == currentSelected) {
                        // Wallet exists and is the same as the one we had. Ensure it's selected.
                        adapter.deselectAll()
                        adapter.selectWallet(updatedWallet)
                    } else if (updatedWallet == null) {
                        // The previously selected wallet was deleted.
                        // This will trigger the main observe block to re-select the first available,
                        // or remain in "all wallets" mode if the list is empty.
                        selectedWallet = null
                        adapter.deselectAll()
                }
                    // Stop observing after the first update to avoid multiple selections
                    // if the wallet LiveData emits again.
                    // This is a common pattern to avoid unintended side effects.
                    viewmodel.getWalletById(currentSelected.id).removeObservers(viewLifecycleOwner)
            }
        }
    }


    private fun setupClickListeners() {
        binding.cardViewAddTransaction.setOnClickListener {
            if (selectedWallet == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.choose_one_wallet), Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = TransactionAddActivity.newIntent(requireContext(), selectedWallet!!.id)
                startActivity(intent)
            }
        }

        binding.textViewTransactionList.setOnClickListener {
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