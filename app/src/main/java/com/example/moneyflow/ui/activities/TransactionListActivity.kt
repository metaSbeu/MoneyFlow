package com.example.moneyflow.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyflow.R
import com.example.moneyflow.ui.adapters.TransactionAdapter
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.databinding.ActivityTransactionListBinding
import kotlin.random.Random

class TransactionListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionListBinding
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpInsets()

        val transactions = mutableListOf<Transaction>()
        repeat(200) {
            transactions.add(
                Transaction(
                    categoryId = TODO(),
                    walletId = TODO(),
                    sum = (it * 1000) + 1000,
                    isIncome = Random.nextBoolean(),
                    note = "Note $it",
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        adapter = TransactionAdapter()
        adapter.transactions = transactions
        binding.recyclerViewTransactions.adapter = adapter
    }

    fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, TransactionListActivity::class.java)
            return intent
        }
    }
}