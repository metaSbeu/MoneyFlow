package com.example.moneyflow.ui.activities

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.data.Plan
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.utils.getDrawableResId
import com.example.moneyflow.databinding.ActivityTransactionEditBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.TransactionEditViewModel
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.text.TextWatcher
import android.text.Editable
import java.text.NumberFormat

class TransactionEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionEditBinding
    private lateinit var viewModel: TransactionEditViewModel
    private var transactionId: Int = 0

    private lateinit var expenseAdapter: CategoryAdapter
    private lateinit var incomeAdapter: CategoryAdapter

    private var isIncomeSelected = false
    private var selectedDateInMillis = System.currentTimeMillis()
    private lateinit var selectedCategory: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0)
        viewModel = ViewModelProvider(this)[TransactionEditViewModel::class.java]
        viewModel.getTransactionById(transactionId)

        observeViewModel()
        setupAdapters()
        setupSumEditTextValidation() // Добавляем валидацию для поля суммы

        binding.recyclerViewExpenseCategories.adapter = expenseAdapter
        binding.recyclerViewIncomeCategories.adapter = incomeAdapter

        binding.buttonSave.setOnClickListener {
            val sumText = binding.editTextNewSum.text.toString()
            if (sumText.isBlank()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sum = sumText.toDoubleOrNull()
            if (sum == null || sum <= 0) {
                Toast.makeText(this, "Введите корректную сумму больше 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentTransaction = viewModel.transaction.value ?: return@setOnClickListener
            val updatedTransaction = currentTransaction.copy(
                sum = sum,
                note = binding.editTextNewComment.text.toString(),
                createdAt = selectedDateInMillis,
                categoryId = selectedCategory.id
            )
            viewModel.updateTransaction(updatedTransaction)
        }
        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.imageViewChangeWallet.setOnClickListener {
            showWalletSelectionDialog()
        }
        binding.buttonDate.setOnClickListener {
            datePicker()
        }
    }

    private fun setupSumEditTextValidation() {
        binding.editTextNewSum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let { editable ->
                    val text = editable.toString()
                    if (text.startsWith("0") && text.length > 1 && !text.startsWith("0.")) {
                        editable.delete(0, 1)
                    }
                    if (text == ".") {
                        editable.clear()
                    }
                }
            }
        })
    }

    fun datePicker() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()
        datePicker.show(supportFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            selectedDateInMillis = selection

            val locale = resources.configuration.locales[0]
            val dateFormat = SimpleDateFormat("d MMM", locale)
            val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"

            binding.textViewDate.text = formattedDate
        }
    }

    fun setTransactionDate() {
        viewModel.transaction.value?.let { transaction ->
            val dateInMillis = transaction.createdAt
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dateInMillis

            val locale = resources.configuration.locales[0]
            val dateFormat = SimpleDateFormat("d MMM", locale)
            val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"
            binding.textViewDate.text = formattedDate
            selectedDateInMillis = dateInMillis
        }
    }

    private fun showWalletSelectionDialog() {
        viewModel.getAllWallets { wallets ->
            val walletNames = wallets.map { it.name }
            val currentWalletId = viewModel.transaction.value?.walletId ?: 0

            AlertDialog.Builder(this)
                .setTitle("Выберите кошелек")
                .setSingleChoiceItems(
                    walletNames.toTypedArray(),
                    wallets.indexOfFirst { it.id == currentWalletId }
                ) { dialog, which ->
                    val selectedWallet = wallets[which]
                    updateWalletInTransaction(selectedWallet)
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.removal))
            .setMessage("Вы уверены что хотите удалить запланированный расход?")
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                viewModel.deleteTransaction(transactionId)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateWalletInTransaction(wallet: Wallet) {
        val iconResId = baseContext.getDrawableResId(wallet.icon)
        binding.imageViewWalletIcon.setImageResource(iconResId)
        val formatted = wallet.balance.formatWithSpaces(this)
        binding.textViewWalletName.text = getString(R.string.wallet_main_info, wallet.name, formatted)
        viewModel.setSelectedWalletId(wallet.id)
    }

    fun setupAdapters() {
        expenseAdapter = CategoryAdapter(
            onItemClick = {
                selectedCategory = it
            },
            onAddClick = {
                startActivity(CategoryAddActivity.newIntent(this, false))
            },
            showAddButton = true,
            isIncome = false,
            onFirstCategorySelected = {
                selectedCategory = it
            }
        )

        incomeAdapter = CategoryAdapter(
            onItemClick = {
                selectedCategory = it
            },
            onAddClick = {
                startActivity(CategoryAddActivity.newIntent(this, true))
            },
            showAddButton = true,
            isIncome = true,
            onFirstCategorySelected = {
                selectedCategory = it
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCategories()
    }

    private fun observeViewModel() {
        viewModel.transaction.observe(this) { transaction ->
            val numberFormat = NumberFormat.getInstance(Locale.getDefault())
            numberFormat.isGroupingUsed = false // Отключаем группировку разрядов
            binding.editTextNewSum.setText(numberFormat.format(transaction.sum))
            binding.editTextNewComment.setText(transaction.note)

            isIncomeSelected = transaction.isIncome
            selectedDateInMillis = transaction.createdAt
            setTransactionDate()
            if (transaction.isIncome) {
                binding.recyclerViewIncomeCategories.visibility = View.VISIBLE
                binding.recyclerViewExpenseCategories.visibility = View.GONE
            } else {
                binding.recyclerViewIncomeCategories.visibility = View.GONE
                binding.recyclerViewExpenseCategories.visibility = View.VISIBLE
            }

            viewModel.getCategoryById(transaction.categoryId) { category ->
                category?.let {
                    viewModel.getWalletById(transaction.walletId) { wallet ->
                        wallet?.let {
                            setupOldTransaction(transaction, it, category)
                        }
                    }
                }
            }
        }

        viewModel.categories.observe(this) { categories ->
            val expenseCategories = categories.filter { !it.isIncome }
            val incomeCategories = categories.filter { it.isIncome }

            expenseAdapter.categories = expenseCategories
            incomeAdapter.categories = incomeCategories

            viewModel.transaction.value?.let { transaction ->
                val targetCategories =
                    if (transaction.isIncome) incomeCategories else expenseCategories
                selectedCategory = targetCategories.find { it.id == transaction.categoryId }
                    ?: targetCategories.firstOrNull()
                            ?: return@let
            }
        }

        viewModel.shouldCloseScreen.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
            }
        }
    }

    private fun setupOldTransaction(transaction: Transaction, wallet: Wallet, category: Category) {
        val sign = if (transaction.isIncome) "+" else "-"
        val formattedTransactionSum = transaction.sum.formatWithSpaces(this)
        binding.textViewOldSum.text = "$sign$formattedTransactionSum"
        binding.textViewOldSum.setTextColor(
            ContextCompat.getColor(
                this,
                if (transaction.isIncome) R.color.light_green else R.color.light_red
            )
        )

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.textViewOldDate.text = "Дата: ${dateFormat.format(Date(transaction.createdAt))}"

        binding.textViewOldComment.text = "Комментарий: ${transaction.note ?: "нет"}"

        binding.textViewOldCategoryName.text = category.name
        var iconResId = baseContext.getDrawableResId(category.icon)
        binding.imageViewCategoryOldIcon.setImageResource(iconResId)

        iconResId = baseContext.getDrawableResId(wallet.icon)
        binding.imageViewWalletIcon.setImageResource(iconResId)
        val formattedWalletBalance = wallet.balance.formatWithSpaces(this)
        binding.textViewWalletName.text = getString(R.string.wallet_main_info, wallet.name, formattedWalletBalance)
    }

    companion object {
        const val EXTRA_TRANSACTION_ID = "transaction_id"

        fun newIntent(context: Context, transactionId: Int): Intent {
            val intent = Intent(context, TransactionEditActivity::class.java)
            intent.putExtra(EXTRA_TRANSACTION_ID, transactionId)
            return intent
        }
    }
}