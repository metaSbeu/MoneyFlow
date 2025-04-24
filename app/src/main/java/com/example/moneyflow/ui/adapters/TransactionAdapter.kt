package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.TransactionWithCategory
import kotlin.time.Duration.Companion.nanoseconds

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
): RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    var transactions = listOf<TransactionWithCategory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        val transactionWithCategory = transactions[position]
        val transaction = transactionWithCategory.transaction
        val category = transactionWithCategory.category

        holder.imageViewIcon.setImageResource(category.iconResId)
        holder.textViewCategory.text = category.name
        holder.textViewAmount.text = transaction.sum.toString()

        val colorResId = when(transaction.isIncome) {
            true -> R.color.light_green
            false -> R.color.light_red
        }
        val color = ContextCompat.getColor(holder.itemView.context, colorResId)
        holder.textViewAmount.setTextColor(color)

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon = itemView.findViewById<ImageView>(R.id.imageViewCategoryIcon)
        val textViewCategory = itemView.findViewById<TextView>(R.id.textViewCategoryName)
        val textViewAmount = itemView.findViewById<TextView>(R.id.textViewSum)
    }
}