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

class TransactionAdapter: RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    var transactions = mutableListOf<Transaction>()
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
        val transaction = transactions[position]

        holder.imageViewIcon.setImageResource(R.drawable.ic_cafe)
        holder.textViewCategory.text = transaction.categoryId.toString()
        holder.textViewAmount.text = transaction.sum.toString()

        val colorResId = when(transaction.isIncome) {
            true -> R.color.light_green
            false -> R.color.light_red
        }
        val color = ContextCompat.getColor(holder.itemView.context, colorResId)
        holder.textViewAmount.setTextColor(color)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon = itemView.findViewById<ImageView>(R.id.imageViewIBankIcon)
        val textViewCategory = itemView.findViewById<TextView>(R.id.textViewCategory)
        val textViewAmount = itemView.findViewById<TextView>(R.id.textViewAmount)
    }
}