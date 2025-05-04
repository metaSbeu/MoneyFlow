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
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.Wallet
import com.example.moneyflow.utils.getDrawableResId
import com.example.moneyflow.databinding.ActivityTransactionEditBinding
import com.example.moneyflow.ui.adapters.CategoryAdapter
import com.example.moneyflow.ui.viewmodels.TransactionEditViewModel
import com.example.moneyflow.utils.setupBottomViewKeyboardVisibilityListener
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Получаем ID транзакции из Intent
        transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0)
        viewModel = ViewModelProvider(this)[TransactionEditViewModel::class.java]

        // Загружаем транзакцию для редактирования
        viewModel.getTransactionById(transactionId)

        // Наблюдаем за данными
        observeViewModel()
        setupAdapters()

        binding.recyclerViewExpenseCategories.adapter = expenseAdapter
        binding.recyclerViewIncomeCategories.adapter = incomeAdapter

        // Сохраняем изменения
        binding.buttonSave.setOnClickListener {
            val sumText = binding.editTextNewSum.text.toString()
            if (sumText.isBlank()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Получаем текущую транзакцию
            val currentTransaction = viewModel.transaction.value ?: return@setOnClickListener

            // Создаем обновленную транзакцию с новыми данными
            val updatedTransaction = currentTransaction.copy(
                sum = sumText.toDouble(),
                note = binding.editTextNewComment.text.toString(),
                createdAt = selectedDateInMillis,
                categoryId = selectedCategory.id // Добавляем обновленную категорию
            )

            viewModel.updateTransaction(updatedTransaction)
        }
        binding.buttonDelete.setOnClickListener {
            viewModel.deleteTransaction(transactionId)
        }

        binding.imageViewChangeWallet.setOnClickListener {
            showWalletSelectionDialog()
        }
        binding.buttonDate.setOnClickListener {
            datePicker()
        }
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
            val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
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
            val dateFormat = SimpleDateFormat("d MMM yyyy", locale) // Убрал лишний символ
            val formattedDate = "Дата: ${dateFormat.format(calendar.time)}"
            binding.textViewDate.text = formattedDate
            selectedDateInMillis = dateInMillis // Обновляем selectedDateInMillis
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

    private fun updateWalletInTransaction(wallet: Wallet) {
        // Обновляем отображение кошелька
        val iconResId = baseContext.getDrawableResId(wallet.icon)
        binding.imageViewWalletIcon.setImageResource(iconResId)
        binding.textViewWalletName.text = wallet.name
        binding.textViewWalletBalance.text = "%.2f ₽".format(wallet.balance)
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
            // Заполняем поля для редактирования
            binding.editTextNewSum.setText(transaction.sum.toString())
            binding.editTextNewComment.setText(transaction.note)

            // Определяем тип транзакции (доход/расход)
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

            // Загружаем категорию транзакции
            viewModel.getCategoryById(transaction.categoryId) { category ->
                category?.let {
                    // Загружаем кошелек транзакции
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

            // Устанавливаем выбранную категорию на основе текущей транзакции
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
        // Устанавливаем сумму с правильным знаком и цветом
        val sumText = if (transaction.isIncome) "+${transaction.sum} ₽" else "-${transaction.sum} ₽"
        binding.textViewOldSum.text = sumText
        binding.textViewOldSum.setTextColor(
            ContextCompat.getColor(
                this,
                if (transaction.isIncome) R.color.light_green else R.color.light_red
            )
        )

        // Устанавливаем дату в формате "dd.MM.yyyy"
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.textViewOldDate.text = "Дата: ${dateFormat.format(Date(transaction.createdAt))}"

        // Устанавливаем комментарий
        binding.textViewOldComment.text = "Комментарий: ${transaction.note ?: "нет"}"

        // Устанавливаем данные категории
        binding.textViewOldCategoryName.text = category.name
        var iconResId = baseContext.getDrawableResId(category.icon)
        binding.imageViewCategoryOldIcon.setImageResource(iconResId)

        // Устанавливаем данные кошелька
        iconResId = baseContext.getDrawableResId(wallet.icon)
        binding.imageViewWalletIcon.setImageResource(iconResId)
        binding.textViewWalletName.text = wallet.name
        binding.textViewWalletBalance.text = "%.2f ₽".format(wallet.balance)
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

