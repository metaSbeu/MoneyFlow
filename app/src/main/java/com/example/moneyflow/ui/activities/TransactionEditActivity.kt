package com.example.moneyflow.ui.activities

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.R
import com.example.moneyflow.databinding.ActivityTransactionEditBinding
import com.example.moneyflow.ui.viewmodels.TransactionEditViewModel

class TransactionEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionEditBinding
    private lateinit var viewModel: TransactionEditViewModel
    private var transactionId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTransactionEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        // Получаем ID транзакции из Intent
        transactionId = intent.getIntExtra(EXTRA_TRANSACTION_ID, 0)
        viewModel = ViewModelProvider(this)[TransactionEditViewModel::class.java]

        // Загружаем транзакцию для редактирования
        viewModel.getTransactionById(transactionId)

        // Наблюдаем за данными
        observeViewModel()

        // Сохраняем изменения
        binding.buttonSave.setOnClickListener {
            val sumText = binding.editTextNewSum.text.toString()
            if (sumText.isBlank()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTransaction = viewModel.transaction.value?.copy(
                sum = sumText.toDouble(),
                note = binding.editTextNewComment.text.toString()
            )

            updatedTransaction?.let {
                viewModel.updateTransaction(it)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.transaction.observe(this) { transaction ->
            if (transaction != null) {
                binding.editTextNewSum.setText(transaction.sum.toString())
                binding.editTextNewComment.setText(transaction.note)
                // Устанавливаем категорию или другие данные
            }
        }

        viewModel.shouldCloseScreen.observe(this) { shouldClose ->
            if (shouldClose) {
                finish()
            }
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
